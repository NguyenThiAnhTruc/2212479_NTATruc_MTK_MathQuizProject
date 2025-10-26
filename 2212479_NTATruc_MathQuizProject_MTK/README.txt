GitHub:https://github.com/NguyenThiAnhTruc/2212479_NTATruc_MTK_MathQuizProject  

Cách chạy:
Để chạy ứng dụng Math Quiz:
Tạo cơ sở dữ liệu MathQuiz trong SQL Server bằng script SQLQuery1.sql.
Cấu hình file config.properties đúng thông tin SQL.
Chạy lệnh: mvn exec:java -Dexec.mainClass="com.mycompany.mathgame.app.MainUI"
Ứng dụng khởi động với giao diện Java Swing, cho phép chơi và xem bảng xếp hạng lưu trên SQL Server.

Math Quiz Project
====================================================

1. Giới thiệu

Math Quiz là ứng dụng trò chơi luyện tập Toán học được viết bằng Java, giúp người chơi rèn luyện các phép tính cơ bản như cộng, trừ, nhân, chia thông qua các câu hỏi ngẫu nhiên.
Ứng dụng áp dụng các mẫu thiết kế hướng đối tượng (Design Patterns) gồm Strategy, Decorator, và Composite, giúp dễ mở rộng và bảo trì. Cơ sở dữ liệu SQL Server, cho phép lưu trữ và truy xuất kết quả nhanh hơn, ổn định hơn.
2. Công nghệ sử dụng

Ngôn ngữ: Java

Giao diện: Java Swing

Quản lý thư viện: Apache Maven

Cơ sở dữ liệu: Microsoft SQL Server 2019 trở lên

Thư viện JDBC: mssql-jdbc (phiên bản 12.6.1.jre11)

IDE khuyến nghị: NetBeans / IntelliJ IDEA

Hệ điều hành: Windows 10 hoặc Windows 11
3. Cấu trúc thư mục dự án
2212479_NTATruc_MathQuizProject_MTK
│
├── Database/
│   └── SQLQuery1.sql                → Tệp tạo và khởi tạo cơ sở dữ liệu SQL Server (MathQuiz)
│
├── docs/                            → Thư mục lưu tài liệu báo cáo, sơ đồ UML, hình minh họa (nếu có)
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/mycompany/mathgame/
│       │       ├── app/
│       │       │   └── MainUI.java               → Lớp chính, khởi động giao diện ứng dụng
│       │       │
│       │       ├── core/                         → Chứa các lớp xử lý logic trung tâm của trò chơi
│       │       │
│       │       ├── db/
│       │       │   └── DatabaseConnection.java   → Lớp kết nối SQL Server (đọc config.properties)
│       │       │
│       │       ├── leaderboard/                  → Quản lý bảng xếp hạng (Leaderboard)
│       │       │   ├── LeaderboardEntry.java
│       │       │   ├── LeaderboardServiceDb.java
│       │       │   └── LeaderboardPort.java
│       │       │
│       │       ├── strategy/                     → Các mẫu thiết kế (Design Patterns)
│       │       │   ├── difficulty/               → Chiến lược thay đổi độ khó (DifficultyStrategy)
│       │       │   ├── question/                 → Chiến lược sinh câu hỏi (QuestionStrategy)
│       │       │   └── score/                    → Chiến lược tính điểm (ScoreStrategy)
│       │       │
│       │       └── ui/                           → Giao diện người dùng (Java Swing Panels)
│       │           ├── LeaderboardPanel.java     → Hiển thị bảng xếp hạng
│       │           └── GamePanel.java (nếu có)   → Màn hình chính chơi game
│       │
│       └── resources/
│           ├── config.properties                 → File cấu hình kết nối SQL Server
│           ├── logo.png                          → Biểu tượng ứng dụng
│           └── sodolop.png                       → Hình sơ đồ hoặc minh họa giao diện
│
├── test/                            → Thư mục cho các lớp kiểm thử (JUnit)
│
├── target/                          → Thư mục Maven build (tự sinh khi biên dịch)
│
├── README.txt                       → Tệp hướng dẫn sử dụng và cấu trúc dự án
│
└── pom.xml                          → Tệp cấu hình Maven, khai báo thư viện sử dụng

4.Chức năng chính

Sinh câu hỏi ngẫu nhiên theo cấp độ (Strategy Pattern).

Tính điểm và thưởng chuỗi đúng liên tiếp (Decorator Pattern).

Điều chỉnh độ khó tự động theo khả năng (StreakBasedDifficulty).

Lưu bảng xếp hạng vào cơ sở dữ liệu SQL Server.

Giao diện thân thiện, hỗ trợ chế độ sáng/tối.

Quản lý dữ liệu thông qua SQL Server.
5. Hướng phát triển

Mở rộng loại câu hỏi và chế độ chơi (Thử thách thời gian, Học theo cấp độ).

Tích hợp âm thanh, hiệu ứng và biểu đồ thống kê.

Hỗ trợ đa ngôn ngữ (tiếng Việt – tiếng Anh).

