import sqlite3
from werkzeug.security import generate_password_hash

def create_admin_account(username, password):
    conn = sqlite3.connect('bank.db')
    cursor = conn.cursor()
    
    # 將密碼進行哈希處理以確保安全性
    hashed_password = generate_password_hash(password)
    
    try:
        cursor.execute('''
            INSERT INTO users (username, password, role, balance)
            VALUES (?, ?, 'ADMIN', 0.0)
        ''', (username, hashed_password))
        conn.commit()
        print(f"Admin account '{username}' created successfully.")
    except sqlite3.IntegrityError as e:
        print(f"Error: {e}")
    finally:
        conn.close()

if __name__ == '__main__':
    # 替換以下的 username 和 password 為你希望的管理者帳號資訊
    create_admin_account('admin', 'password')
