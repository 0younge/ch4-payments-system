-- 상품 더미데이터
INSERT INTO products (category, name, description, price, stock, status, created_at, updated_at) VALUES
('전자기기', '상품1', '상품1 설명', 10000, 10, 'ON_SALE', NOW(), NOW()),
('전자기기', '상품2', '상품2 설명', 20000, 5, 'ON_SALE', NOW(), NOW()),
('의류', '상품3', '상품3 설명', 30000, 0, 'SOLD_OUT', NOW(), NOW());
