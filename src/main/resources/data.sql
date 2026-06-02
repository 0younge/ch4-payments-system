-- USER

INSERT INTO users
(email, password, name, phoneNumber, pointBalance)
VALUES
    ('test1@test.com', '1234', '길동홍', '01011112222', 10000),

    ('test2@test.com', '1234', '홍길동', '01033334444', 5000),

    ('test3@test.com', '1234', '김철수', '01055556666', 20000);



-- PRODUCT

INSERT INTO products
(name, price, stock, description, category, status)
VALUES

    ('오버핏 코튼 티셔츠', 39000, 100,
     '부드러운 코튼 소재의 오버핏 티셔츠',
     'TOP',
     'ON_SALE'),

    ('와이드 데님 팬츠', 59000, 50,
     '데일리 와이드 청바지',
     'BOTTOM',
     'ON_SALE'),

    ('후드 집업', 79000, 30,
     '기본 후드 집업',
     'OUTER',
     'ON_SALE'),

    ('러닝화', 99000, 20,
     '쿠셔닝이 좋은 러닝화',
     'SHOES',
     'ON_SALE'),

    ('슬링백', 45000, 10,
     '가벼운 슬링백',
     'BAG',
     'ON_SALE'),

    ('기본 반팔 티셔츠', 19000, 0,
     '품절 상품 테스트',
     'TOP',
     'SOLD_OUT');



-- CART

INSERT INTO carts
(user_id)
VALUES
    (1),
    (2),
    (3);



-- CART_ITEM

INSERT INTO cart_items
(cart_id, product_id, quantity)
VALUES

-- 길동홍 장바구니
(1, 1, 2),
(1, 2, 1),

-- 홍길동 장바구니
(2, 3, 1),
(2, 4, 1),

-- 김철수 장바구니
(3, 5, 2);



-- POINT HISTORY

INSERT INTO point_histories
(user_id, order_id, pointType, amount)
VALUES

    (1, NULL, 'EARN', 10000),
    (2, NULL, 'EARN', 5000),
    (3, NULL, 'EARN', 20000);