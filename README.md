# Online Auction System

## 1. Gioi Thieu

Online Auction System la du an bai tap lon mon Lap trinh nang cao, xay dung mot he thong dau gia truc tuyen theo mo hinh Client-Server.

He thong cho phep nguoi dung dang ky, dang nhap, dang san pham dau gia, duyet phien dau gia, dat gia, tu dong dau gia, quan ly vi tien va theo doi lich su dau gia theo thoi gian thuc.

Pham vi hien tai cua he thong:

- Client JavaFX cho Bidder, Seller va Admin.
- Server socket xu ly request/response va realtime event.
- SQLite de luu user, auction, bid history va trang thai he thong.
- Quan ly trang thai phien dau gia: `OPEN`, `RUNNING`, `FINISHED`, `PAID`, `CANCELED`.
- Realtime update khi co thay doi ve auction/user va scheduler cap nhat trang thai auction theo thoi gian.

## 2. Cong Nghe Su Dung

- Java 17
- JavaFX 21.0.2
- Maven
- SQLite JDBC
- Gson
- JUnit 5
- Java Socket
- GitHub Actions CI

## 3. Yeu Cau Cai Dat

Can cai san:

- JDK 17 tro len
- Maven
- Git
- Internet trong lan build dau tien de Maven tai dependencies

Kiem tra moi truong:

```bash
java -version
mvn -version
git --version
```

Project su dung JavaFX thong qua Maven dependency trong `pom.xml`, vi vay khong can cai JavaFX SDK rieng.

## 4. Cau Truc Thu Muc

```text
Bai_Tap_Lon/
├── README.md
├── class-diagram.mmd
└── online_auction_system/
    ├── pom.xml
    ├── auction_system.db
    ├── logs/
    └── src/
        ├── main/
        │   ├── java/com/uet/
        │   │   ├── client/
        │   │   │   ├── core/           # MainClient, ClientSocket
        │   │   │   ├── controllers/    # Controller cho JavaFX UI
        │   │   │   ├── data/           # Du lieu dia chi
        │   │   │   └── utils/          # SceneManager, SessionManager, MessageHelper
        │   │   ├── domain/
        │   │   │   ├── contract/       # Biddable, Payable
        │   │   │   ├── entity/         # User, Item, Auction, BidTransaction
        │   │   │   ├── enums/          # AuctionStatus, BidStatus, ItemStatus, ItemType
        │   │   │   ├── event/          # ServerEvent realtime
        │   │   │   ├── factory/        # ItemFactory va cac factory con
        │   │   │   ├── request/        # DTO request gui len server
        │   │   │   ├── result/         # DTO result tra ve client
        │   │   │   └── summary/        # DTO tom tat hien thi UI
        │   │   └── server/
        │   │       ├── core/           # AuctionServer, ClientHandler
        │   │       ├── repositories/   # Truy van SQLite
        │   │       ├── services/       # AuctionManager, AuthenticationService
        │   │       └── utils/          # DatabaseConnection
        │   └── resources/com/uet/
        │       ├── css/                # style.css
        │       └── views/              # FXML views
        └── test/java/com/uet/          # Unit test JUnit
```

## 5. Build Va Test

Neu dang o thu muc goc repo `Bai_Tap_Lon`, chay:

```bash
mvn -B package --file online_auction_system/pom.xml
```

Neu da di vao thu muc project Maven:

```bash
cd online_auction_system
mvn -B package
```

Lenh tren se:

- compile source code
- copy resource FXML/CSS/anh/du lieu
- chay unit test
- dong goi file `.jar` trong thu muc `target`

Neu thay `BUILD SUCCESS` thi project build va test thanh cong.

## 6. Huong Dan Chay Chuong Trinh

Tat ca lenh ben duoi dung duoc tren macOS, Linux va Windows PowerShell, mien la may da cai JDK 17 va Maven.

### 6.1. Clone Project

```bash
git clone https://github.com/bachvu-0607/Bai_Tap_Lon.git
cd Bai_Tap_Lon/online_auction_system
```

### 6.2. Chay Server Truoc

Mo terminal thu nhat:

```bash
mvn exec:java -Dexec.mainClass=com.uet.server.core.AuctionServer
```

Server mac dinh chay o port:

```text
8080
```

Khi server chay thanh cong se thay log tuong tu:

```text
Server started at 8080
```

### 6.3. Chay Client Sau

Mo terminal thu hai:

```bash
mvn javafx:run
```

Neu client va server cung chay tren mot may, nhap IP:

```text
localhost
```

hoac:

```text
127.0.0.1
```

### 6.4. Chay Nhieu Client

Mo them terminal khac va chay lai:

```bash
mvn javafx:run
```

Moi cua so client se tao mot ket noi socket rieng toi server. Cach nay dung de demo nhieu nguoi cung dau gia va realtime update.

### 6.5. Chay Qua Mang LAN

Neu may A chay server va may B chay client:

1. May A chay server bang lenh o muc 6.2.
2. May B chay client bang lenh o muc 6.3.
3. Client tren may B nhap IP LAN cua may A.

Vi du IP may A la:

```text
192.168.1.10
```

thi client tren may B nhap:

```text
192.168.1.10
```

Luu y:

- Hai may can cung mang LAN.
- Firewall cua may chay server khong duoc chan port `8080`.
- Server log co the hien IP khac nhau tuy client ket noi tu dia chi mang nao.

## 7. Tai Khoan Demo

Khi server khoi dong, he thong seed mot so tai khoan demo neu chua ton tai:

| Vai tro | Citizen ID | Mat khau |
| --- | --- | --- |
| Admin | `026207002258` | `Bach123` |
| Seller | `026207002259` | `Bach123` |
| Bidder | `026207002260` | `Bach123` |
| Bidder | `026207002261` | `Bach123` |

Man hinh dang nhap hien tai dung Phone Number/ID va Password. Co the dang nhap bang so dien thoai/citizen ID theo logic cua he thong.

## 8. Chuc Nang Da Hoan Thanh

### 8.1. Chuc Nang Bat Buoc

| Yeu cau | Trang thai | Noi dung da thuc hien |
| --- | --- | --- |
| Quan ly nguoi dung Bidder/Seller/Admin | Hoan thanh | Dang ky, dang nhap theo role, validate phone/citizen ID, quan ly user, ban user |
| Quan ly san pham dau gia CRUD | Hoan thanh | Seller dang san pham, nhap thong tin san pham, xem san pham cua minh, Admin duyet/tu choi phien |
| Tham gia dau gia | Hoan thanh | Bidder xem auction, dat gia, kiem tra gia hop le, cap nhat current price/current winner realtime |
| Ket thuc phien dau gia | Hoan thanh | Server tu cap nhat trang thai theo thoi gian, khoa phien het han, xac dinh winner, ho tro thanh toan |
| Xu ly loi va ngoai le | Hoan thanh | Xu ly sai mat khau, trung tai khoan, user bi ban, bid khong hop le, thieu tien, socket timeout |
| Giao dien GUI JavaFX | Hoan thanh | Co giao dien Sign In, Register, Home, Auction List, Post Product, My Product, Wallet, Admin Approval |
| Thiet ke OOP | Hoan thanh | Co ke thua, da hinh, truu tuong, dong goi qua entity, factory, service, repository, request/result DTO |
| Design Patterns | Hoan thanh | Singleton cho AuctionManager, Factory cho Item, Observer/event notify cho realtime update |
| Kien truc Client-Server MVC | Hoan thanh | JavaFX Client, Socket Server, Controller tach UI, Service xu ly nghiep vu, Repository xu ly database |
| Xu ly dau gia dong thoi | Hoan thanh | Server xu ly nhieu client bang thread, cac thao tac bid quan trong duoc synchronized |
| Unit Test va CI/CD | Hoan thanh | Co JUnit test va GitHub Actions CI build/test Maven project |

### 8.2. Chuc Nang Nang Cao

| Chuc nang | Trang thai | Noi dung da thuc hien |
| --- | --- | --- |
| Auto-Bidding | Hoan thanh | Bidder co the bat/tat tu dong dau gia theo gioi han tien toi da |
| Anti-sniping | Hoan thanh | Neu co bid gan thoi diem ket thuc, phien dau gia tu dong gia han them thoi gian |
| Bid History Visualization | Hoan thanh | Hien thi lich su bid va bieu do gia realtime trong panel chi tiet auction |

### 8.3. Cac Luong Nghiep Vu Chinh

- Seller dang san pham dau gia, Admin duyet, sau do auction moi hien tren danh sach cua Bidder.
- Bidder dat gia, server kiem tra trang thai phien, buoc gia toi thieu va so du vi truoc khi chap nhan.
- Khi co bid hop le, he thong cap nhat `currentPrice`, `highestBidder`, `bidHistory` va gui realtime event ve cac client.
- Neu bidder moi vuot gia bidder cu, he thong hoan tien tam giu cho bidder cu va giu tien cua bidder dang dan dau.
- Server tu kiem tra trang thai auction moi 3 giay de chuyen `OPEN` sang `RUNNING` va `RUNNING` sang `FINISHED`.
- Khi Admin ban user, server ngat ket noi user do, cap nhat danh sach online va xu ly lai auction neu user do dang la highest bidder.
- Khi server khoi dong lai, he thong load user, auction va bid history tu SQLite de tiep tuc trang thai da luu.

### 8.4. Database Va Logging

- SQLite luu user, auction va bid history.
- Server load auction va bid history tu database khi khoi dong lai.
- Server ghi log bang Java Logger.
- File log duoc tao tu dong tai:

```text
online_auction_system/logs/auction-system.log
```

### 8.5. Testing Va CI

- Co unit test cho entity, item factory, user account, auction va bid transaction.
- Co GitHub Actions CI build/test Maven project.
- Lenh CI dung:

```bash
mvn -B package --file online_auction_system/pom.xml
```

## 9. Tinh Huong Demo De Kiem Thu

- Chay server va mo hai client cung luc.
- Bidder A dat gia, Bidder B thay auction tu cap nhat realtime.
- Bidder B dat gia cao hon, Bidder A duoc hoan tien tam giu.
- Dat gia gan thoi diem ket thuc de kiem tra tu dong gia han.
- Seller dang san pham, Admin duyet, Bidder thay auction xuat hien.
- Admin ban user dang online, client cua user do nhan thong bao va bi ngat ket noi.
- Tat server, mo lai server, du lieu auction va bid history van duoc load tu SQLite.

## 10. Database

Project dung SQLite local. File database nam trong:

```text
online_auction_system/auction_system.db
```

Khi server khoi dong, code se tu tao bang neu chua co. Khong can cai MySQL/PostgreSQL.

## 11. Logging

Server dung Java `Logger` va ghi log ra file:

```text
online_auction_system/logs/auction-system.log
```

File log duoc tao tu dong khi chay `AuctionServer`. Neu chua co thu muc `logs`, server se tu tao.

## 12. GitHub Actions CI

Workflow CI can dat trong:

```text
.github/workflows/maven.yml
```

Noi dung build quan trong:

```yaml
- name: Build with Maven
  run: mvn -B package --file online_auction_system/pom.xml
```

Neu GitHub Actions hien `Status: Success` thi project da compile, test va package thanh cong tren moi truong GitHub.

## 13. Link Bao Cao Va Video Demo

- Bao cao PDF: [Google Drive](https://drive.google.com/file/d/1XNk6p5o1Y9u54hu3d5_t7NsHpbhvm67q/view?usp=share_link)
- Video demo: [Google Drive](https://drive.google.com/file/d/1Ei8s3Msfg1kG0d-Lb0sSjN4KC1OB0oot/view?usp=share_link)

## 14. Loi Thuong Gap

### Khong tim thay `pom.xml`

Neu dang o root repo, dung:

```bash
mvn -B package --file online_auction_system/pom.xml
```

Neu da `cd online_auction_system`, dung:

```bash
mvn -B package
```

### Client bao khong ket noi duoc server

Kiem tra:

- Server da chay chua.
- Client nhap dung IP server chua.
- Server dang dung port `8080`.
- Hai may co cung mang LAN khong.
- Firewall co chan port `8080` khong.

### JavaFX warning

Mot so warning JavaFX/JDK co the xuat hien khi chay app. Neu app van mo va chay binh thuong thi co the bo qua.

### GitHub Actions bao loi khong co `pom.xml`

Nguyen nhan la workflow dang chay Maven o root repo trong khi `pom.xml` nam trong `online_auction_system`.

Sua lenh build thanh:

```bash
mvn -B package --file online_auction_system/pom.xml
```
