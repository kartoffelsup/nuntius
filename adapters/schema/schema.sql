CREATE TABLE nuntius_user (
    uuid character(36) primary key,
    username varchar(32) UNIQUE not null,
    pw bytea not null,
    email varchar(32) UNIQUE not null,
    created_at timestamptz not null,
    last_login timestamptz
)

CREATE TABLE user_contact (
    user_id character(36) NOT NULL,
    contact_id character(36) NOT NULL,

    PRIMARY KEY(user_id, contact_id),
    FOREIGN KEY(user_id)
        REFERENCES nuntius_user (uuid),
    FOREIGN KEY(contact_id)
        REFERENCES nuntius_user (uuid)
)

CREATE TABLE user_notification (
    user_id character(36) PRIMARY KEY,
    token text NOT NULL,
    updated_at timestamptz not null,

        FOREIGN KEY(user_id)
            REFERENCES nuntius_user (uuid)
)

CREATE TABLE message_queue (
    message_id character(36) primary key,
    recipient character(36) not null,
    sender character(36) not null,
    payload    bytea not null,
    time_of_server_arrival timestamptz not null,

    FOREIGN KEY(recipient)
        REFERENCES nuntius_user (uuid),
    FOREIGN KEY(sender)
        REFERENCES nuntius_user (uuid)
)