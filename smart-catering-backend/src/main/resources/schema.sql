CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(128) UNIQUE NOT NULL,
    nickname VARCHAR(64),
    avatar_url VARCHAR(255),
    phone VARCHAR(32),
    status VARCHAR(20) DEFAULT 'NORMAL',
    last_login_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dish_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    sort INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ENABLE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dish (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    category_id BIGINT,
    name VARCHAR(64) NOT NULL,
    image_url VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    description VARCHAR(255),
    stock INT DEFAULT 0,
    sales_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ON',
    taste_tags VARCHAR(255),
    spicy_options VARCHAR(255),
    size_options VARCHAR(255),
    recommended TINYINT DEFAULT 0,
    recommend_weight INT DEFAULT 0,
    version INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dish_spec (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dish_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    sort INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ON',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dining_table (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    table_no VARCHAR(32) NOT NULL,
    seats INT DEFAULT 4,
    status VARCHAR(32) DEFAULT 'FREE',
    qr_token VARCHAR(128),
    qr_content VARCHAR(255),
    qr_update_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dining_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    table_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    pay_status VARCHAR(32) DEFAULT 'UNPAID',
    total_amount DECIMAL(10,2) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dining_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    dish_id BIGINT NOT NULL,
    dish_name VARCHAR(64),
    price DECIMAL(10,2),
    quantity INT NOT NULL,
    spicy VARCHAR(32),
    size VARCHAR(32),
    amount DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    table_id BIGINT NOT NULL,
    table_no VARCHAR(32),
    contact_name VARCHAR(64) NOT NULL,
    contact_phone VARCHAR(32) NOT NULL,
    reservation_date DATE NOT NULL,
    reservation_time TIME NOT NULL,
    party_size INT NOT NULL,
    status VARCHAR(32) DEFAULT 'PENDING',
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_dish_store_status ON dish(store_id, status);
CREATE INDEX idx_dish_spec_dish ON dish_spec(dish_id, status, sort);
CREATE INDEX idx_order_user_time ON dining_order(user_id, create_time);
CREATE INDEX idx_order_store_status ON dining_order(store_id, status);
CREATE INDEX idx_order_item_order ON dining_order_item(order_id);
CREATE INDEX idx_reservation_user_time ON reservation(user_id, reservation_date, reservation_time);
CREATE INDEX idx_reservation_store_status ON reservation(store_id, status, reservation_date);
