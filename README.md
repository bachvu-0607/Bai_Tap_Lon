# Online Auction System

Dự án Bài tập lớn Lập trình nâng cao - Hệ thống đấu giá client-server bằng Java, JavaFX, Socket, SQLite và Maven.

## Yêu Cầu Môi Trường

Cần cài sẵn:

- JDK 17
- Maven
- Git
- Internet ở lần build đầu tiên để Maven tải thư viện

Kiểm tra:

```bash
java -version
mvn -version
git --version
```

Project dùng Maven và JavaFX dependency trong `pom.xml`, nên không cần cài JavaFX riêng.

## Clone Project

```bash
git clone https://github.com/bachvu-0607/Bai_Tap_Lon.git
cd Bai_Tap_Lon/online_auction_system
```

## Build Và Test

```bash
mvn -B package
```

Lệnh này sẽ:

- compile code Java
- copy resources như FXML, CSS, ảnh, JSON
- chạy JUnit test
- đóng gói file `.jar` trong thư mục `target`

Nếu thấy `BUILD SUCCESS` là project build/test ổn.

## Chạy Server

Mở terminal thứ nhất:

```bash
cd Bai_Tap_Lon/online_auction_system
mvn exec:java -Dexec.mainClass=com.uet.server.core.AuctionServer
```

Server mặc định mở ở port:

```text
8080
```

Khi server chạy thành công sẽ thấy log kiểu:

```text
Server started at 8080
```

## Chạy Client

Mở terminal thứ hai:

```bash
cd Bai_Tap_Lon/online_auction_system
mvn javafx:run
```

Nếu server và client chạy trên cùng một máy, nhập IP:

```text
localhost
```

hoặc:

```text
127.0.0.1
```

## Chạy Nhiều Client

Muốn mở nhiều client trên cùng máy, mở thêm terminal và chạy lại:

```bash
cd Bai_Tap_Lon/online_auction_system
mvn javafx:run
```

Mỗi cửa sổ client sẽ tạo một kết nối socket riêng tới server.

## Chạy Qua Mạng LAN

Nếu máy A chạy server và máy B chạy client:

1. Máy A chạy server.
2. Máy B chạy client.
3. Client trên máy B nhập IP LAN của máy A.

Ví dụ IP máy A là:

```text
192.168.1.10
```

thì client nhập:

```text
192.168.1.10
```

Lưu ý:

- Hai máy cần cùng mạng LAN.
- Firewall của máy chạy server không được chặn port `8080`.
- Server log có thể hiện IP khác nhau tùy máy client kết nối từ địa chỉ mạng nào.

## Database

Project dùng SQLite local. File database nằm trong project:

```text
online_auction_system/auction_system.db
```

Khi server khởi động, code sẽ tạo bảng nếu chưa có. Không cần cài MySQL/PostgreSQL.

## Logging

Server dùng Java `Logger` và ghi log ra file:

```text
online_auction_system/logs/auction-system.log
```

File log này được tạo tự động khi chạy `AuctionServer`. Trong code server có:

```java
Files.createDirectories(Path.of("logs"));
FileHandler fileHandler = new FileHandler("logs/auction-system.log", true);
```

Nghĩa là:

- nếu chưa có thư mục `logs`, server tự tạo
- nếu chưa có file `auction-system.log`, server tự tạo
- tham số `true` nghĩa là log mới được ghi nối tiếp vào cuối file, không xóa log cũ

Nên không cần tự tạo file log thủ công. Chỉ cần chạy server là file sẽ xuất hiện sau khi logger được setup và có log được ghi.

## CI Trên GitHub

Workflow GitHub Actions dùng Maven để kiểm tra project trên máy GitHub.

Lệnh CI nên dùng:

```bash
mvn -B package --file online_auction_system/pom.xml
```

Nếu GitHub Actions báo:

```text
Status: Success
build
```

thì nghĩa là project đã compile, test và package thành công trên môi trường GitHub.

## Lỗi Thường Gặp

### Không tìm thấy `pom.xml`

Nếu chạy ở root repo thì phải dùng:

```bash
mvn -B package --file online_auction_system/pom.xml
```

Nếu đã `cd online_auction_system` rồi thì dùng:

```bash
mvn -B package
```

### Client báo không kết nối được server

Kiểm tra:

- Server đã chạy chưa.
- Client nhập đúng IP server chưa.
- Server đang dùng port `8080`.
- Hai máy có cùng mạng LAN không.
- Firewall có chặn port `8080` không.

### JavaFX warning

Một số warning JavaFX/JDK có thể xuất hiện khi chạy app. Nếu app vẫn mở và chạy bình thường thì có thể bỏ qua.
