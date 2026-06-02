-- USER
INSERT INTO users
(email, password, name, phone_number, point_balance, created_at, updated_at)
VALUES
    ('test1@test.com', '1234', '길동홍', '01011112222', 10000, NOW(), NOW()),
    ('test2@test.com', '1234', '홍길동', '01033334444', 5000, NOW(), NOW()),
    ('test3@test.com', '1234', '김철수', '01055556666', 20000, NOW(), NOW());



-- PRODUCT
INSERT INTO products
(name, price, stock, description, category, status, created_at, updated_at)
VALUES
    ('오버핏 코튼 티셔츠', 39000, 100, '부드러운 코튼 소재의 오버핏 티셔츠', 'TOP', 'ON_SALE', NOW(), NOW()),
    ('와이드 데님 팬츠', 59000, 50, '데일리 와이드 청바지', 'BOTTOM', 'ON_SALE', NOW(), NOW()),
    ('후드 집업', 79000, 30, '기본 후드 집업', 'OUTER', 'ON_SALE', NOW(), NOW()),
    ('러닝화', 99000, 20, '쿠셔닝이 좋은 러닝화', 'SHOES', 'ON_SALE', NOW(), NOW()),
    ('슬링백', 45000, 10, '가벼운 슬링백', 'BAG', 'ON_SALE', NOW(), NOW()),
    ('기본 반팔 티셔츠', 19000, 0, '품절 상품 테스트', 'TOP', 'SOLD_OUT', NOW(), NOW());



-- CART
INSERT INTO carts
(user_id, created_at, updated_at)
VALUES
    (1, NOW(), NOW()),
    (2, NOW(), NOW()),
    (3, NOW(), NOW());



-- CART_ITEM
INSERT INTO cart_items
(cart_id, product_id, quantity, created_at, updated_at)
VALUES
-- 길동홍 장바구니
(1, 1, 2, NOW(), NOW()),
(1, 2, 1, NOW(), NOW()),
-- 홍길동 장바구니
(2, 3, 1, NOW(), NOW()),
(2, 4, 1, NOW(), NOW()),
-- 김철수 장바구니
(3, 5, 2, NOW(), NOW());



-- POINT HISTORY
INSERT INTO point_histories
(user_id, order_id, point_type, amount, balance_after, description, created_at)
VALUES
    (1, NULL, 'EARN', 10000, 10000, '포인트 적립', NOW()),
    (2, NULL, 'EARN', 5000, 5000, '포인트 적립', NOW()),
    (3, NULL, 'EARN', 20000, 20000, '포인트 적립', NOW());
