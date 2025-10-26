/* =========================================================================
    MathQuiz – Full Setup + Demo Seed (SQL Server)
    - Tạo DB, schema, tables, indexes, views, procs, synonym, phân quyền
    - Gộp luôn seed demo 2 người chơi + vài phiên chơi mẫu
   ========================================================================= */

-----------------------------------------
-- 0) DATABASE
-----------------------------------------
IF DB_ID(N'MathQuiz') IS NULL
BEGIN
    PRINT 'Creating database MathQuiz...';
    CREATE DATABASE MathQuiz;
END
GO
USE MathQuiz;
GO

-----------------------------------------
-- 1) SCHEMA
-----------------------------------------
IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = N'quiz')
BEGIN
    PRINT 'Creating schema quiz...';
    EXEC('CREATE SCHEMA quiz');
END
GO

-----------------------------------------
-- 2) TABLES
-----------------------------------------
IF OBJECT_ID('quiz.Players','U') IS NULL
BEGIN
    CREATE TABLE quiz.Players (
        PlayerId      INT IDENTITY(1,1) PRIMARY KEY,
        PlayerName    NVARCHAR(100) NOT NULL UNIQUE,
        CreatedAt     DATETIME2 NOT NULL CONSTRAINT DF_Players_CreatedAt DEFAULT (SYSUTCDATETIME())
    );
    PRINT 'Created table quiz.Players';
END
GO

IF OBJECT_ID('quiz.GameSession','U') IS NULL
BEGIN
    CREATE TABLE quiz.GameSession (
        SessionId     BIGINT IDENTITY(1,1) PRIMARY KEY,
        PlayerId      INT         NOT NULL,
        StartedAt     DATETIME2   NOT NULL CONSTRAINT DF_Session_StartedAt DEFAULT (SYSUTCDATETIME()),
        EndedAt       DATETIME2       NULL,
        TotalScore    INT         NOT NULL CONSTRAINT DF_Session_TotalScore DEFAULT (0),
        TotalTimeMs   BIGINT      NOT NULL CONSTRAINT DF_Session_TotalTime DEFAULT (0),
        MaxStreak     INT         NOT NULL CONSTRAINT DF_Session_MaxStreak  DEFAULT (0),
        CONSTRAINT FK_Session_Player FOREIGN KEY (PlayerId) REFERENCES quiz.Players(PlayerId)
    );
    PRINT 'Created table quiz.GameSession';
END
GO

IF OBJECT_ID('quiz.Question','U') IS NULL
BEGIN
    CREATE TABLE quiz.Question (
        QuestionId    BIGINT IDENTITY(1,1) PRIMARY KEY,
        Prompt        NVARCHAR(255) NOT NULL,
        Answer        INT           NOT NULL,
        StrategyCode  VARCHAR(32)   NOT NULL
    );
    PRINT 'Created table quiz.Question';
END
GO

IF OBJECT_ID('quiz.Attempt','U') IS NULL
BEGIN
    CREATE TABLE quiz.Attempt (
        AttemptId       BIGINT IDENTITY(1,1) PRIMARY KEY,
        SessionId       BIGINT      NOT NULL,
        QuestionId      BIGINT      NOT NULL,
        Level           INT         NOT NULL,
        SubmittedAnswer INT             NULL,
        IsCorrect       BIT         NOT NULL,
        ElapsedMs       BIGINT      NOT NULL,
        EarnedScore     INT         NOT NULL,
        CreatedAt       DATETIME2   NOT NULL CONSTRAINT DF_Attempt_CreatedAt DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT FK_Attempt_Session  FOREIGN KEY (SessionId) REFERENCES quiz.GameSession(SessionId),
        CONSTRAINT FK_Attempt_Question FOREIGN KEY (QuestionId) REFERENCES quiz.Question(QuestionId)
    );
    PRINT 'Created table quiz.Attempt';
END
GO

-----------------------------------------
-- 3) INDEXES
-----------------------------------------
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Session_PlayerId' AND object_id = OBJECT_ID('quiz.GameSession'))
    CREATE INDEX IX_Session_PlayerId ON quiz.GameSession(PlayerId);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Attempt_SessionId' AND object_id = OBJECT_ID('quiz.Attempt'))
    CREATE INDEX IX_Attempt_SessionId ON quiz.Attempt(SessionId);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Attempt_QuestionId' AND object_id = OBJECT_ID('quiz.Attempt'))
    CREATE INDEX IX_Attempt_QuestionId ON quiz.Attempt(QuestionId);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Question_Strategy' AND object_id = OBJECT_ID('quiz.Question'))
    CREATE INDEX IX_Question_Strategy ON quiz.Question(StrategyCode);
GO

-----------------------------------------
-- 4) VIEWS
-----------------------------------------
CREATE OR ALTER VIEW quiz.v_Leaderboard_AllTime
AS
SELECT TOP (1000)
    s.SessionId,
    p.PlayerName,
    s.TotalScore,
    s.TotalTimeMs,
    s.MaxStreak,
    s.StartedAt,
    s.EndedAt
FROM quiz.GameSession s
JOIN quiz.Players p ON p.PlayerId = s.PlayerId
ORDER BY s.TotalScore DESC, s.TotalTimeMs ASC, s.SessionId DESC;
GO

CREATE OR ALTER VIEW quiz.v_PlayerStats
AS
SELECT
    p.PlayerId,
    p.PlayerName,
    COUNT(s.SessionId)            AS Sessions,
    ISNULL(MAX(s.TotalScore), 0)  AS BestScore,
    ISNULL(MAX(s.MaxStreak), 0)   AS BestStreak
FROM quiz.Players p
LEFT JOIN quiz.GameSession s ON s.PlayerId = p.PlayerId
GROUP BY p.PlayerId, p.PlayerName;
GO

-----------------------------------------
-- 5) STORED PROCEDURES
-----------------------------------------
CREATE OR ALTER PROCEDURE quiz.sp_UpsertPlayer
    @PlayerName NVARCHAR(100),
    @PlayerId   INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @id INT = (SELECT PlayerId FROM quiz.Players WHERE PlayerName = @PlayerName);
    IF @id IS NULL
    BEGIN
        INSERT INTO quiz.Players(PlayerName) VALUES (@PlayerName);
        SET @id = SCOPE_IDENTITY();
    END
    SET @PlayerId = @id;
END
GO

CREATE OR ALTER PROCEDURE quiz.sp_BeginSession
    @PlayerId  INT,
    @SessionId BIGINT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO quiz.GameSession(PlayerId) VALUES (@PlayerId);
    SET @SessionId = SCOPE_IDENTITY();
END
GO

CREATE OR ALTER PROCEDURE quiz.sp_RecordAttempt
    @SessionId       BIGINT,
    @Prompt          NVARCHAR(255),
    @Answer          INT,
    @StrategyCode    VARCHAR(32),
    @Level           INT,
    @SubmittedAnswer INT = NULL,
    @IsCorrect       BIT,
    @ElapsedMs       BIGINT,
    @EarnedScore     INT,
    @CurrentStreak   INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @QuestionId BIGINT;
    SELECT @QuestionId = q.QuestionId
    FROM quiz.Question q
    WHERE q.Prompt = @Prompt AND q.Answer = @Answer AND q.StrategyCode = @StrategyCode;

    IF @QuestionId IS NULL
    BEGIN
        INSERT INTO quiz.Question(Prompt, Answer, StrategyCode)
        VALUES (@Prompt, @Answer, @StrategyCode);
        SET @QuestionId = SCOPE_IDENTITY();
    END

    INSERT INTO quiz.Attempt(SessionId, QuestionId, Level, SubmittedAnswer, IsCorrect, ElapsedMs, EarnedScore)
    VALUES (@SessionId, @QuestionId, @Level, @SubmittedAnswer, @IsCorrect, @ElapsedMs, @EarnedScore);

    UPDATE quiz.GameSession
    SET TotalScore  = TotalScore + @EarnedScore,
        TotalTimeMs = TotalTimeMs + @ElapsedMs,
        MaxStreak   = CASE WHEN @CurrentStreak > MaxStreak THEN @CurrentStreak ELSE MaxStreak END
    WHERE SessionId = @SessionId;
END
GO

CREATE OR ALTER PROCEDURE quiz.sp_EndSession
    @SessionId BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE quiz.GameSession
    SET EndedAt = SYSUTCDATETIME()
    WHERE SessionId = @SessionId AND EndedAt IS NULL;
END
GO

-- Xóa toàn bộ bảng xếp hạng (chỉ xóa session/attempt; giữ Players)
CREATE OR ALTER PROCEDURE quiz.sp_ClearLeaderboard
AS
BEGIN
    SET NOCOUNT ON;
    DELETE FROM quiz.Attempt;
    DELETE FROM quiz.GameSession;
END
GO

-- Xóa mọi phiên của một người chơi theo tên
CREATE OR ALTER PROCEDURE quiz.sp_DeleteSessionsByPlayerName
    @PlayerName NVARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;

    DELETE A
    FROM quiz.Attempt A
    WHERE A.SessionId IN (
        SELECT S.SessionId
        FROM quiz.GameSession S
        JOIN quiz.Players P ON P.PlayerId = S.PlayerId
        WHERE P.PlayerName = @PlayerName
    );

    DELETE S
    FROM quiz.GameSession S
    JOIN quiz.Players P ON P.PlayerId = S.PlayerId
    WHERE P.PlayerName = @PlayerName;
END
GO

-----------------------------------------
-- 6) SYNONYM (tương thích code gọi quiz.Player)
-----------------------------------------
IF OBJECT_ID('quiz.Player','SN') IS NULL
BEGIN
    CREATE SYNONYM quiz.Player FOR quiz.Players;
    PRINT 'Created synonym quiz.Player -> quiz.Players';
END
GO

-----------------------------------------
-- 7) PHÂN QUYỀN (role + grants)
-----------------------------------------
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'mathquiz_app' AND type = 'R')
BEGIN
    CREATE ROLE mathquiz_app;
    PRINT 'Created role mathquiz_app';
END
GO

GRANT SELECT, INSERT, UPDATE, DELETE ON OBJECT::quiz.Players     TO mathquiz_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON OBJECT::quiz.GameSession TO mathquiz_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON OBJECT::quiz.Question    TO mathquiz_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON OBJECT::quiz.Attempt     TO mathquiz_app;
GRANT SELECT ON OBJECT::quiz.v_Leaderboard_AllTime TO mathquiz_app;
GRANT SELECT ON OBJECT::quiz.v_PlayerStats         TO mathquiz_app;
GRANT EXECUTE ON SCHEMA::quiz TO mathquiz_app;  -- EXEC mọi proc trong schema quiz
GO
-- (Tuỳ chọn) Gán user DB cụ thể vào role này:
--   USE MathQuiz;
--   CREATE USER [mathquiz] FOR LOGIN [mathquiz];
--   EXEC sp_addrolemember N'mathquiz_app', N'mathquiz';

-----------------------------------------
-- 8) DEMO SEED (Người chơi A/B + vài phiên)
--    (Có thể chạy lại nhiều lần: Upsert Player nên không trùng)
-----------------------------------------
DECLARE @PlayerIdA INT, @PlayerIdB INT;

EXEC quiz.sp_UpsertPlayer N'Người chơi A', @PlayerIdA OUTPUT;
EXEC quiz.sp_UpsertPlayer N'Người chơi B', @PlayerIdB OUTPUT;

-- Thêm vài phiên chơi cho A
INSERT INTO quiz.GameSession (PlayerId, StartedAt, EndedAt, TotalScore, TotalTimeMs, MaxStreak)
VALUES
(@PlayerIdA, SYSUTCDATETIME(), SYSUTCDATETIME(), 120, 180000, 5),
(@PlayerIdA, SYSUTCDATETIME(), SYSUTCDATETIME(),  95, 220000, 4);

-- Thêm vài phiên chơi cho B
INSERT INTO quiz.GameSession (PlayerId, StartedAt, EndedAt, TotalScore, TotalTimeMs, MaxStreak)
VALUES
(@PlayerIdB, SYSUTCDATETIME(), SYSUTCDATETIME(), 150, 210000, 6),
(@PlayerIdB, SYSUTCDATETIME(), SYSUTCDATETIME(),  80, 260000, 3);

-- Xem leaderboard mẫu
SELECT TOP 10 * FROM quiz.v_Leaderboard_AllTime;
GO

PRINT 'MathQuiz – full setup + demo seed completed.';
