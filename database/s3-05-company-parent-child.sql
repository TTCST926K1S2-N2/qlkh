-- Bổ sung cột parent_id vào bảng customer (hoặc company) để lưu ID công ty mẹ
ALTER TABLE customer 
ADD COLUMN parent_id INT NULL;

-- Tạo khóa ngoại liên kết parent_id tới id của chính bảng đó
ALTER TABLE customer 
ADD CONSTRAINT fk_customer_parent 
FOREIGN KEY (parent_id) REFERENCES customer(id) ON DELETE SET NULL;