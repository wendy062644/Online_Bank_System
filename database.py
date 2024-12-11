import sqlite3
from werkzeug.security import generate_password_hash

def modify_database_structure():
    """
    修改資料庫結構，確保 balance 欄位為整數。
    如果表不存在，則自動創建。
    """
    conn = sqlite3.connect('bank.db')
    cursor = conn.cursor()
    
    try:
        # 檢查是否已經存在表
        cursor.execute("PRAGMA table_info(users);")
        columns = cursor.fetchall()
        
        # 如果表不存在或需要調整結構
        if not columns:
            print("Creating the users table...")
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL,
                    balance INTEGER DEFAULT 0,
                    is_frozen INTEGER DEFAULT 0
                )
            ''')
        else:
            # 檢查是否需要修改 balance 欄位
            column_dict = {column[1]: column[2] for column in columns}
            if column_dict.get("balance") != "INTEGER":
                print("Altering the balance column to INTEGER...")
                cursor.execute("ALTER TABLE users RENAME TO users_backup;")
                
                # 創建新表
                cursor.execute('''
                    CREATE TABLE users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        role TEXT NOT NULL,
                        balance INTEGER DEFAULT 0,
                        is_frozen INTEGER DEFAULT 0
                    )
                ''')
                
                # 從備份表中導入資料並轉換 balance 為整數
                cursor.execute('''
                    INSERT INTO users (id, username, password, role, balance, is_frozen)
                    SELECT id, username, password, role, CAST(balance AS INTEGER), is_frozen
                    FROM users_backup
                ''')
                
                cursor.execute("DROP TABLE users_backup;")
        
        conn.commit()
        print("Database structure updated successfully.")
    except Exception as e:
        print(f"Error updating database structure: {e}")
    finally:
        conn.close()

def create_admin_account(username, password):
    """
    創建管理者帳號，預設餘額為 0。
    """
    conn = sqlite3.connect('bank.db')
    cursor = conn.cursor()
    
    hashed_password = generate_password_hash(password)
    
    try:
        cursor.execute('''
            INSERT INTO users (username, password, role, balance)
            VALUES (?, ?, 'ADMIN', 0)
        ''', (username, hashed_password))
        conn.commit()
        print(f"Admin account '{username}' created successfully.")
    except sqlite3.IntegrityError as e:
        print(f"Error: {e}")
    finally:
        conn.close()

if __name__ == '__main__':
    # 修改資料庫結構
    modify_database_structure()
    
    # 創建管理者帳號
    create_admin_account('admin', 'password')
