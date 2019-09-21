create table if not exists customer (
    id BIGSERIAL PRIMARY KEY,
    document VARCHAR(200) NOT NULL,
    email VARCHAR(200) NOT NULL,
    phone VARCHAR(200) NOT NULL,
    create_date TIMESTAMP NOT NULL
);