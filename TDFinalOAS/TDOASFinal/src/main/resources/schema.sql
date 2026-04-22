
DB_URL=jdbc:postgresql://localhost:5432/federation_db
DB_USERNAME=federation_users
DB_PASSWORD=federation_pass

CREATE USER federation_users WITH PASSWORD 'federation_pass';

GRANT ALL PRIVILEGES ON DATABASE federation_db TO federation_users;
GRANT ALL PRIVILEGES ON SCHEMA public TO federation_users;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL PRIVILEGES ON TABLES TO federation_users;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL PRIVILEGES ON SEQUENCES TO federation_users;

ALTER TABLE collectivities
    ADD CONSTRAINT fk_president      FOREIGN KEY (president_id)      REFERENCES members(id),
    ADD CONSTRAINT fk_vice_president FOREIGN KEY (vice_president_id) REFERENCES members(id),
    ADD CONSTRAINT fk_treasurer      FOREIGN KEY (treasurer_id)      REFERENCES members(id),
    ADD CONSTRAINT fk_secretary      FOREIGN KEY (secretary_id)      REFERENCES members(id);

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

CREATE TABLE contributions (
                               id              VARCHAR(36) PRIMARY KEY,
                               collectivity_id VARCHAR(36) NOT NULL,
                               type            VARCHAR(20) NOT NULL CHECK (type IN ('MONTHLY', 'ANNUAL', 'PUNCTUAL')),
                               amount          BIGINT      NOT NULL,
                               label           VARCHAR(255),
                               FOREIGN KEY (collectivity_id) REFERENCES collectivities(id)
);
CREATE TABLE contribution_payments (
                                       id              VARCHAR(36) PRIMARY KEY,
                                       member_id       VARCHAR(36) NOT NULL,
                                       contribution_id VARCHAR(36) NOT NULL,
                                       amount          BIGINT      NOT NULL,
                                       paid_at         DATE        NOT NULL DEFAULT CURRENT_DATE,
                                       method          VARCHAR(20) NOT NULL CHECK (method IN ('CASH', 'MOBILE_MONEY', 'BANK_TRANSFER')),
                                       FOREIGN KEY (member_id)       REFERENCES members(id),
                                       FOREIGN KEY (contribution_id) REFERENCES contributions(id)
);
CREATE TABLE accounts (
                          id              VARCHAR(36) PRIMARY KEY,
                          collectivity_id VARCHAR(36) NOT NULL,
                          type            VARCHAR(20) NOT NULL CHECK (type IN ('CASH', 'BANK', 'MOBILE_MONEY')),
                          holder_name     VARCHAR(150) NOT NULL,
                          balance         BIGINT       NOT NULL DEFAULT 0,
                          bank_name       VARCHAR(50),
                          account_number  VARCHAR(23),
                          provider        VARCHAR(30),
                          phone_number    VARCHAR(20),
                          FOREIGN KEY (collectivity_id) REFERENCES collectivities(id)
);

CREATE TABLE activities (
                            id              VARCHAR(36) PRIMARY KEY,
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

CREATE TABLE member_referees (
                                 member_id  VARCHAR(36) NOT NULL,
                                 referee_id VARCHAR(36) NOT NULL,
                                 PRIMARY KEY (member_id, referee_id),
                                 FOREIGN KEY (member_id)  REFERENCES members(id),
                                 FOREIGN KEY (referee_id) REFERENCES members(id)
);

ALTER TABLE collectivities
    ADD COLUMN number VARCHAR(20) UNIQUE,
    ADD COLUMN name   VARCHAR(150) UNIQUE;


DROP TABLE IF EXISTS attendance CASCADE;
DROP TABLE IF EXISTS activity_target_roles CASCADE;
DROP TABLE IF EXISTS activities CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS contribution_payments CASCADE;
DROP TABLE IF EXISTS contributions CASCADE;
DROP TABLE IF EXISTS registration_fees CASCADE;
DROP TABLE IF EXISTS mandates CASCADE;
DROP TABLE IF EXISTS member_sponsors CASCADE;
DROP TABLE IF EXISTS member_referees CASCADE;
DROP TABLE IF EXISTS members CASCADE;
DROP TABLE IF EXISTS collectivities CASCADE;

CREATE TABLE collectivities (
                                id                  VARCHAR(36)  PRIMARY KEY,
                                number              VARCHAR(20)  UNIQUE,
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

CREATE TABLE contributions (
                               id              VARCHAR(36) PRIMARY KEY,
                               collectivity_id VARCHAR(36) NOT NULL,
                               type            VARCHAR(20) NOT NULL CHECK (type IN ('MONTHLY', 'ANNUAL', 'PUNCTUAL')),
                               amount          BIGINT      NOT NULL,
                               label           VARCHAR(255),
                               FOREIGN KEY (collectivity_id) REFERENCES collectivities(id)
);

CREATE TABLE contribution_payments (
                                       id              VARCHAR(36) PRIMARY KEY,
                                       member_id       VARCHAR(36) NOT NULL,
                                       contribution_id VARCHAR(36) NOT NULL,
                                       amount          BIGINT      NOT NULL,
                                       paid_at         DATE        NOT NULL DEFAULT CURRENT_DATE,
                                       method          VARCHAR(20) NOT NULL CHECK (method IN ('CASH', 'MOBILE_MONEY', 'BANK_TRANSFER')),
                                       FOREIGN KEY (member_id)       REFERENCES members(id),
                                       FOREIGN KEY (contribution_id) REFERENCES contributions(id)
);

CREATE TABLE accounts (
                          id              VARCHAR(36)  PRIMARY KEY,
                          collectivity_id VARCHAR(36)  NOT NULL,
                          type            VARCHAR(20)  NOT NULL CHECK (type IN ('CASH', 'BANK', 'MOBILE_MONEY')),
                          holder_name     VARCHAR(150) NOT NULL,
                          balance         BIGINT       NOT NULL DEFAULT 0,
                          bank_name       VARCHAR(50),
                          account_number  VARCHAR(23),
                          provider        VARCHAR(30),
                          phone_number    VARCHAR(20),
                          FOREIGN KEY (collectivity_id) REFERENCES collectivities(id)
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