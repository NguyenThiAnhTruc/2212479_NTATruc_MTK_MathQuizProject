GitHub:https://github.com/NguyenThiAnhTruc/2212479_NTATruc_MTK_MathQuizProject  

Math Quiz Project
====================================================
1. Giới thiệu

Math Quiz là ứng dụng trò chơi luyện tập Toán học được viết bằng Java, giúp người chơi rèn luyện các phép tính cơ bản như cộng, trừ, nhân, chia thông qua các câu hỏi ngẫu nhiên.
Ứng dụng áp dụng các mẫu thiết kế hướng đối tượng (Design Patterns) gồm Strategy, Decorator, và Composite, giúp dễ mở rộng và bảo trì. Cơ sở dữ liệu SQL Server, cho phép lưu trữ và truy xuất kết quả nhanh hơn, ổn định hơn.
2. Công nghệ sử dụng

Ngôn ngữ: Java SE 17

Giao diện: Java Swing

Quản lý thư viện: Apache Maven

Cơ sở dữ liệu: Microsoft SQL Server 2019 trở lên

Thư viện JDBC: mssql-jdbc (phiên bản 12.6.1.jre11)

IDE khuyến nghị: NetBeans / IntelliJ IDEA

Hệ điều hành: Windows 10 hoặc Windows 11
3.Chức năng chính

Sinh câu hỏi ngẫu nhiên theo cấp độ (Strategy Pattern).

Tính điểm và thưởng chuỗi đúng liên tiếp (Decorator Pattern).

Điều chỉnh độ khó tự động theo khả năng (StreakBasedDifficulty).

Lưu bảng xếp hạng vào cơ sở dữ liệu SQL Server.

Giao diện thân thiện, hỗ trợ chế độ sáng/tối.

Quản lý dữ liệu qua các Stored Procedure:

sp_UpsertPlayer

sp_BeginSession

sp_RecordAttempt

sp_EndSession
4. Hướng phát triển

Phát triển thêm phiên bản web hoặc di động.

Mở rộng loại câu hỏi và chế độ chơi (Thử thách thời gian, Học theo cấp độ).

Tích hợp âm thanh, hiệu ứng và biểu đồ thống kê.

Hỗ trợ đa ngôn ngữ (tiếng Việt – tiếng Anh).

