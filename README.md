# Dự Án Sửa Lỗi Hardcode URL Và Tối Ưu Hóa RestTemplate

Dự án này giải quyết triệt để lỗi thắt nút cổ chai (bottleneck) và hardcode IP trong giao tiếp đồng bộ giữa `order-service` và `product-service` bằng cách sử dụng Eureka Discovery, Spring Cloud LoadBalancer và cơ chế Fallback thích ứng.

## Cấu Trúc Thư Mục Hệ Thống
```text
order-service/
├── src/
│   ├── main/java/com/vietmart/orderservice/
│   │   ├── client/ProductServiceClientRT.java  (Client gọi API và xử lý lỗi)
│   │   ├── config/RestTemplateConfig.java      (Cấu hình LoadBalanced RestTemplate với Timeout)
│   │   ├── dto/ProductInfo.java                (DTO truyền nhận dữ liệu)
│   │   └── exception/ProductNotFoundException.java (Exception nghiệp vụ 404)
│   └── test/java/com/vietmart/orderservice/
│       └── client/ProductServiceClientRTTest.java  (Unit tests đầy đủ các kịch bản)
└── README.md
```

## Quy Trình Giao Tiếp Giữa Các Service

```mermaid
sequenceDiagram
    autonumber
    participant OrderService as Order Service (Client)
    participant Eureka as Eureka Server (Discovery)
    participant ProductService as Product Service (Instances)

    OrderService->>Eureka: Truy vấn tìm instance hoạt động cho "product-service"
    Eureka-->>OrderService: Trả về danh sách IP thực tế của các instance
    OrderService->>OrderService: Thực hiện Load Balancing và áp dụng Timeout (2s / 3s)
    alt Giao tiếp thành công
        OrderService->>ProductService: GET http://product-service/api/products/{id}
        ProductService-->>OrderService: 200 OK với ProductInfo
    else Timeout xảy ra (ResourceAccessException)
        OrderService->>OrderService: Trigger Fallback và trả về thông tin mặc định
    else Sản phẩm không tồn tại (404 NotFound)
        OrderService->>OrderService: Ném ProductNotFoundException
    end
```

## Cách Chạy Unit Test
Để thực thi kiểm thử và đảm bảo hoạt động đúng đắn của logic client:
```bash
mvn clean test
```
