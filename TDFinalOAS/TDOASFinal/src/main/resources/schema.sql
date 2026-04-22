DB_URL=jdbc:postgresql://localhost:5432/federation_db
DB_USERNAME=federation_users
DB_PASSWORD=federation_pass

-- CREATE USER federation_users WITH PASSWORD 'federation_pass';
-- GRANT ALL PRIVILEGES ON DATABASE federation_db TO federation_users;
-- GRANT ALL PRIVILEGES ON SCHEMA public TO federation_users;
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO federation_users;
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO federation_users;

DROP TABLE IF EXISTS attendance CASCADE;
DROP TABLE IF EXISTS activity_target_roles CASCADE;
DROP TABLE IF EXISTS activities CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS member_payments CASCADE;
DROP TABLE IF EXISTS membership_fees CASCADE;
DROP TABLE IF EXISTS financial_accounts CASCADE;
DROP TABLE IF EXISTS registration_fees CASCADE;
DROP TABLE IF EXISTS mandates CASCADE;
DROP TABLE IF EXISTS member_sponsors CASCADE;
DROP TABLE IF EXISTS member_referees CASCADE;
DROP TABLE IF EXISTS members CASCADE;
DROP TABLE IF EXISTS collectivities CASCADE;

CREATE TABLE collectivities (
    id                  VARCHAR(36)  PRIMARY KEY,
    number              INT          UNIQUE,
    name                VARCHAR(150) UNIQUE,
    location            VARCHAR(150) NOT NULL,
    specialty           VARCHAR(150) NOT NULL,
    created_at          DATE         NOT NULL DEFAULT CURRENT_DATE,
    president_id        VARCHAR(36),
    vice_president_id   VARCHAR(36),
    treasurer_id        VARCHAR(36),
    secretary_id        VARCHAR(36)
);

CREATE TABLE members (
    id              VARCHAR(36)  PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    birth_date      DATE         NOT NULL,
    gender          VARCHAR(10)  NOT NULL CHECK (gender IN ('MALE', 'FEMALE')),
    address         VARCHAR(255) NOT NULL,
    profession      VARCHAR(100) NOT NULL,
    phone_number    BIGINT       NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    occupation      VARCHAR(30)  NOT NULL CHECK (occupation IN (
        'JUNIOR', 'SENIOR', 'SECRETARY',
        'TREASURER', 'VICE_PRESIDENT', 'PRESIDENT'
    )),
    collectivity_id VARCHAR(36),
    joined_at       DATE         NOT NULL DEFAULT CURRENT_DATE,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    FOREIGN KEY (collectivity_id) REFERENCES collectivities(id)
);

ALTER TABLE collectivities
    ADD CONSTRAINT fk_president      FOREIGN KEY (president_id)      REFERENCES members(id),
    ADD CONSTRAINT fk_vice_president FOREIGN KEY (vice_president_id) REFERENCES members(id),
    ADD CONSTRAINT fk_treasurer      FOREIGN KEY (treasurer_id)      REFERENCES members(id),
    ADD CONSTRAINT fk_secretary      FOREIGN KEY (secretary_id)      REFERENCES members(id);

CREATE TABLE member_referees (
    member_id  VARCHAR(36) NOT NULL,
    referee_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (member_id, referee_id),
    FOREIGN KEY (member_id)  REFERENCES members(id),
    FOREIGN KEY (referee_id) REFERENCES members(id)
);

CREATE TABLE member_sponsors (
    candidate_id VARCHAR(36)  NOT NULL,
    sponsor_id   VARCHAR(36)  NOT NULL,
    relationship VARCHAR(100) NOT NULL,
    PRIMARY KEY (candidate_id, sponsor_id),
    FOREIGN KEY (candidate_id) REFERENCES members(id),
    FOREIGN KEY (sponsor_id)   REFERENCES members(id)
);

CREATE TABLE mandates (
    id              VARCHAR(36) PRIMARY KEY,
    member_id       VARCHAR(36) NOT NULL,
    collectivity_id VARCHAR(36) NOT NULL,
    role            VARCHAR(30) NOT NULL CHECK (role IN (
        'PRESIDENT', 'VICE_PRESIDENT', 'TREASURER', 'SECRETARY'
    )),
    year            INT NOT NULL,
    UNIQUE (collectivity_id, role, year),
    FOREIGN KEY (member_id)       REFERENCES members(id),
    FOREIGN KEY (collectivity_id) REFERENCES collectivities(id)
);

CREATE TABLE registration_fees (
    id        VARCHAR(36) PRIMARY KEY,
    member_id VARCHAR(36) NOT NULL UNIQUE,
    amount    BIGINT      NOT NULL DEFAULT 50000,
    paid_at   DATE        NOT NULL DEFAULT CURRENT_DATE,
    method    VARCHAR(20) NOT NULL CHECK (method IN ('MOBILE_MONEY', 'BANK_TRANSFER')),
    FOREIGN KEY (member_id) REFERENCES members(id)
);

CREATE TABLE membership_fees (
    id              VARCHAR(36) PRIMARY KEY,
    collectivity_id VARCHAR(36) NOT NULL,
    eligible_from   DATE        NOT NULL,
    frequency       VARCHAR(20) NOT NULL CHECK (frequency IN ('WEEKLY', 'MONTHLY', 'ANNUALLY', 'PUNCTUALLY')),
    amount          NUMERIC     NOT NULL,
    label           VARCHAR(255),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    FOREIGN KEY (collectivity_id) REFERENCES collectivities(id)
);

CREATE TABLE financial_accounts (
    id                     VARCHAR(36)  PRIMARY KEY,
    collectivity_id        VARCHAR(36)  NOT NULL,
    type                   VARCHAR(20)  NOT NULL CHECK (type IN ('CASH', 'BANK', 'MOBILE_BANKING')),
    balance                NUMERIC      NOT NULL DEFAULT 0,
    -- BankAccount fields
    holder_name            VARCHAR(150),
    bank_name              VARCHAR(50),
    bank_code              INT,
    bank_branch_code       INT,
    bank_account_number    INT,
    bank_account_key       INT,
    -- MobileBankingAccount fields
    mobile_banking_service VARCHAR(50),
    mobile_number          INT,
    FOREIGN KEY (collectivity_id) REFERENCES collectivities(id)
);

CREATE TABLE member_payments (
    id                  VARCHAR(36) PRIMARY KEY,
    member_id           VARCHAR(36) NOT NULL,
    membership_fee_id   VARCHAR(36) NOT NULL,
    account_credited_id VARCHAR(36) NOT NULL,
    amount              NUMERIC     NOT NULL,
    payment_mode        VARCHAR(20) NOT NULL CHECK (payment_mode IN ('CASH', 'MOBILE_BANKING', 'BANK_TRANSFER')),
    creation_date       DATE        NOT NULL DEFAULT CURRENT_DATE,
    FOREIGN KEY (member_id)           REFERENCES members(id),
    FOREIGN KEY (membership_fee_id)   REFERENCES membership_fees(id),
    FOREIGN KEY (account_credited_id) REFERENCES financial_accounts(id)
);

CREATE TABLE transactions (
    id                  VARCHAR(36) PRIMARY KEY,
    collectivity_id     VARCHAR(36) NOT NULL,
    creation_date       DATE        NOT NULL DEFAULT CURRENT_DATE,
    amount              NUMERIC     NOT NULL,
    payment_mode        VARCHAR(20) NOT NULL CHECK (payment_mode IN ('CASH', 'MOBILE_BANKING', 'BANK_TRANSFER')),
    account_credited_id VARCHAR(36) NOT NULL,
    member_debited_id   VARCHAR(36) NOT NULL,
    FOREIGN KEY (collectivity_id)     REFERENCES collectivities(id),
    FOREIGN KEY (account_credited_id) REFERENCES financial_accounts(id),
    FOREIGN KEY (member_debited_id)   REFERENCES members(id)
);

CREATE TABLE activities (
    id              VARCHAR(36)  PRIMARY KEY,
    collectivity_id VARCHAR(36),
    title           VARCHAR(255) NOT NULL,
    type            VARCHAR(40)  NOT NULL CHECK (type IN (
        'MONTHLY_GENERAL_ASSEMBLY',
        'JUNIOR_TRAINING',
        'EXCEPTIONAL',
        'FEDERATION'
    )),
    activity_date   DATE         NOT NULL,
    is_mandatory    BOOLEAN      NOT NULL DEFAULT TRUE,
    FOREIGN KEY (collectivity_id) REFERENCES collectivities(id)
);

CREATE TABLE activity_target_roles (
    activity_id VARCHAR(36) NOT NULL,
    role        VARCHAR(30) NOT NULL CHECK (role IN (
        'JUNIOR', 'SENIOR', 'SECRETARY',
        'TREASURER', 'VICE_PRESIDENT', 'PRESIDENT'
    )),
    PRIMARY KEY (activity_id, role),
    FOREIGN KEY (activity_id) REFERENCES activities(id)
);

CREATE TABLE attendance (
    id                           VARCHAR(36) PRIMARY KEY,
    activity_id                  VARCHAR(36) NOT NULL,
    member_id                    VARCHAR(36) NOT NULL,
    status                       VARCHAR(10) NOT NULL CHECK (status IN ('PRESENT', 'ABSENT', 'EXCUSED')),
    excuse_reason                VARCHAR(255),
    is_from_another_collectivity BOOLEAN     NOT NULL DEFAULT FALSE,
    UNIQUE (activity_id, member_id),
    FOREIGN KEY (activity_id) REFERENCES activities(id),
    FOREIGN KEY (member_id)   REFERENCES members(id)
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO federation_users;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO federation_users;