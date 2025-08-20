# -*- coding: utf-8 -*-

import os
import re
import time
import random
from pathlib import Path
from typing import Optional, Dict, List, Tuple
from urllib.parse import urlparse, quote, unquote

import mysql.connector
from mysql.connector.connection import MySQLConnection
from mysql.connector import Error as MySQLError
from playwright.sync_api import sync_playwright, Page

# -------------------- 환경설정 --------------------
def _bool_env(name: str, default: bool) -> bool:
    v = os.environ.get(name)
    if v is None:
        return default
    return v.strip().lower() in ("1", "true", "yes", "y", "on")

GITHUB = _bool_env("GITHUB_ACTIONS", False)

HEADLESS = _bool_env("HEADLESS", True if GITHUB else False)
SLOW_MO = int(os.environ.get("SLOW_MO_MS", "0" if GITHUB else "80"))
MAX_ROWS = int(os.environ["MAX_ROWS"]) if os.environ.get("MAX_ROWS") else None
CANDIDATE_LIMIT = int(os.environ.get("CANDIDATE_LIMIT", "8"))
DEBUG_SHOT = _bool_env("DEBUG_SHOT", False)

SHOT_DIR = Path("run_shots")
if DEBUG_SHOT:
    SHOT_DIR.mkdir(exist_ok=True)

# DB: github actions secrets (필수)
PROD_DB_URL = os.environ.get("PROD_DB_URL") or ""
PROD_DB_USER = os.environ.get("PROD_DB_USER") or ""
PROD_DB_PASSWORD = os.environ.get("PROD_DB_PASSWORD") or ""

# 테이블/컬럼
TABLE_RESTAURANT = "restaurant"
COL_ID = "restaurant_id"
COL_NAME = "name"
COL_ZIP = "road_zip_code"

TABLE_IMAGE = "image"
IMAGE_CATEGORY = "RESTAURANT"   # enum
IMAGE_TYPE_MAIN = "MAIN"        # enum

# -------------------- 유틸/로그 --------------------
def ts() -> str:
    import datetime as dt
    return dt.datetime.now().strftime("%H:%M:%S")

def info(msg: str) -> None:
    print(f"[[[{ts()}]]] INFO: {msg}")

def warn(msg: str) -> None:
    print(f"[[[{ts()}]]] WARN: {msg}")

def save_shot(page: Page, tag: str) -> None:
    if not DEBUG_SHOT:
        return
    try:
        fp = SHOT_DIR / f"{int(time.time()*1000)}_{tag}.png"
        page.screenshot(path=str(fp), full_page=True)
        print(f"[shot] saved: {fp}")
    except Exception:
        pass

def normalize_zip(s: Optional[str]) -> Optional[str]:
    if not s:
        return None
    m = re.search(r"\b(\d{5})\b", s)
    return m.group(1) if m else None

def parse_db_url(url: str) -> Tuple[str, int, str]:
    """jdbc:mysql://host:port/db, mysql://host:port/db, host:port/db 모두 지원"""
    if not url:
        raise RuntimeError("PROD_DB_URL is empty")
    u = url.strip()
    if u.startswith("jdbc:"):
        u = u[5:]
    if "://" not in u:
        u = "mysql://" + u
    parsed = urlparse(u)
    host = parsed.hostname or "127.0.0.1"
    port = parsed.port or 3306
    db = (parsed.path or "/").lstrip("/") or ""
    if not db:
        raise RuntimeError("DB name not found in PROD_DB_URL")
    return host, port, db

# -------------------- DB --------------------
def get_conn() -> MySQLConnection:
    host, port, dbname = parse_db_url(PROD_DB_URL)
    try:
        conn = mysql.connector.connect(
            host=host,
            port=port,
            user=PROD_DB_USER,
            password=PROD_DB_PASSWORD,
            database=dbname,
            autocommit=False,
        )
        info(f"MySQL 연결 성공: db={dbname} @{host}:{port}")
        return conn
    except MySQLError as e:
        raise RuntimeError(f"MySQL 연결 실패: {e}")

def fetch_restaurants(conn: MySQLConnection) -> List[Tuple[int, str, Optional[str]]]:
    q = f"SELECT {COL_ID}, {COL_NAME}, {COL_ZIP} FROM {TABLE_RESTAURANT}"
    if MAX_ROWS:
        q += f" LIMIT {int(MAX_ROWS)}"
    cur = conn.cursor(buffered=True)
    cur.execute(q)
    rows = cur.fetchall()
    cur.close()
    return rows

def upsert_main_image(conn: MySQLConnection, rid: int, hero_url: Optional[str]) -> None:
    if not hero_url:
        return
    # 완전 덮어쓰기: 기존 MAIN 모두 삭제 후 새로 INSERT (created_at/updated_at 강제세팅)
    try:
        cur = conn.cursor()
        cur.execute(
            f"DELETE FROM {TABLE_IMAGE} WHERE restaurant_id=%s AND category=%s AND type=%s",
            (rid, IMAGE_CATEGORY, IMAGE_TYPE_MAIN),
        )
        cur.execute(
            f"INSERT INTO {TABLE_IMAGE} "
            f"(restaurant_id, category, type, image_url, created_at, updated_at) "
            f"VALUES (%s,%s,%s,%s,NOW(6),NOW(6))",
            (rid, IMAGE_CATEGORY, IMAGE_TYPE_MAIN, hero_url),
        )
        conn.commit()
    except MySQLError as e:
        conn.rollback()
        warn(f"image UPSERT 실패 id={rid}: {e}")
    finally:
        try:
            cur.close()
        except Exception:
            pass

# -------------------- Kakao --------------------
def kakao_search_candidates(page: Page, name: str, max_items: int) -> List[str]:
    page.goto(f"https://map.kakao.com/?q={quote(name)}", wait_until="domcontentloaded")
    page.wait_for_load_state("networkidle")
    save_shot(page, "goto")

    pids: List[str] = []
    try:
        anchors = page.locator("a[href*='place.map.kakao.com/']")
        cnt = anchors.count()
        for i in range(min(cnt, max_items * 5)):
            href = anchors.nth(i).get_attribute("href") or ""
            m = re.search(r"place\.map\.kakao\.com/(\d+)", href)
            if not m:
                continue
            pid = m.group(1)
            if pid not in pids:
                pids.append(pid)
            if len(pids) >= max_items:
                break
    except Exception:
        pass
    return pids

def kakao_extract_zip(page: Page) -> Optional[str]:
    try:
        body = page.locator("body").inner_text()
        m = re.search(r"\(\s*우\s*\)\s*(\d{5})", body)
        return m.group(1) if m else normalize_zip(body)
    except Exception:
        return None

def unwrap_kakao_image_url(src: Optional[str]) -> Optional[str]:
    """ //… 또는 cthumb/local?fname=… → 원본 URL로 복원 """
    if not src:
        return None
    if src.startswith("//"):
        src = "https:" + src
    m = re.search(r"[?&]fname=([^&]+)", src)
    if m:
        try:
            inner = unquote(m.group(1))
            if inner.startswith("//"):
                inner = "https:" + inner
            return inner
        except Exception:
            pass
    return src

def kakao_extract_main_image(page: Page) -> Optional[str]:
    # 사진 영역이 접혀 있으면 열기 시도
    try:
        for sel in ["a:has-text('사진')", "button:has-text('사진')",
                    "a:has-text('전체보기')", "button:has-text('전체보기')"]:
            btn = page.locator(sel).first
            if btn.count() > 0 and btn.is_visible():
                try:
                    btn.click()
                    page.wait_for_timeout(300)
                    break
                except Exception:
                    pass
    except Exception:
        pass

    selectors = [
        "a.link_photo img",     # 대표 갤러리(질문에서 본 구조)
        ".list_photo img",
        ".photo_area img",
        "img.img-thumb",
        "img.thumb_img",
    ]
    bad_parts = ("/staticmap", "/sprite", "/marker/", "/roadview/", "mapjsapi", "/tile/")

    def ok(u: str) -> bool:
        if not u:
            return False
        if any(b in u for b in bad_parts):
            return False
        return True

    try:
        for sel in selectors:
            imgs = page.locator(sel)
            cnt = imgs.count()
            if cnt == 0:
                continue
            for i in range(min(cnt, 24)):
                el = imgs.nth(i)
                src = el.get_attribute("src") or el.get_attribute("data-src") or ""
                src = unwrap_kakao_image_url(src)
                if src and ok(src):
                    return src
    except Exception:
        pass
    return None

def scrape_kakao_place_detail(page: Page, pid: str) -> Dict[str, Optional[str]]:
    url = f"https://place.map.kakao.com/{pid}"
    page.goto(url, wait_until="domcontentloaded")
    page.wait_for_load_state("networkidle")
    save_shot(page, f"kakao_{pid}_loaded")
    return {
        "pid": pid,
        "zip": kakao_extract_zip(page),
        "hero": kakao_extract_main_image(page),
    }

# -------------------- 메인 --------------------
def main() -> None:
    conn = get_conn()
    rows = fetch_restaurants(conn)
    info(f"[run] 대상 레스토랑 수: {len(rows)}")

    with sync_playwright() as pw:
        browser = pw.chromium.launch(
            headless=HEADLESS,
            args=["--disable-blink-features=AutomationControlled"],
            slow_mo=SLOW_MO,
        )
        context = browser.new_context(
            locale="ko-KR",
            user_agent=("Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                        "AppleWebKit/537.36 (KHTML, like Gecko) "
                        "Chrome/124.0 Safari/537.36")
        )
        context.add_init_script(
            "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
        )
        page = context.new_page()

        try:
            for (rid, name, zip_expected) in rows:
                name = (name or "").strip()
                zip_expected = normalize_zip(zip_expected)
                if not name or not zip_expected:
                    continue

                info(f"[search(kakao)] id={rid} name='{name}' zip_db={zip_expected}")
                pids = kakao_search_candidates(page, name, CANDIDATE_LIMIT)
                print(f"[[[{ts()}]]] DBG : candidates {name} -> {pids}")
                if not pids:
                    warn(f"id={rid} name='{name}' -> kakao-no-candidate")
                    continue

                chosen = None
                for pid in pids:
                    detail = scrape_kakao_place_detail(page, pid)
                    if normalize_zip(detail.get("zip")) == zip_expected:
                        chosen = detail
                        break

                if not chosen:
                    warn(f"id={rid} name='{name}' zip={zip_expected} -> 카카오 매칭 실패")
                    continue

                hero = chosen.get("hero")
                ok_photo = 'Y' if hero else 'N'
                info(f"OK  : id={rid} kakao_pid={chosen['pid']} zip={chosen.get('zip') or '-'} photo={ok_photo}")

                upsert_main_image(conn, rid, hero)
                time.sleep(random.uniform(0.4, 0.9))

        finally:
            try:
                page.close()
            except Exception:
                pass
            context.close()
            browser.close()
            conn.close()

if __name__ == "__main__":
    main()
