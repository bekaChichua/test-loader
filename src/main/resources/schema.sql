CREATE TABLE property
(
    id                UUID PRIMARY KEY,
    owner_id          UUID        NOT NULL,
    version           INT         NOT NULL,
    upload_date       TIMESTAMP   NOT NULL,
    type              VARCHAR(50),
    title             VARCHAR(50),
    place_id          VARCHAR(255),
    full_address_name VARCHAR(500),
    country           VARCHAR(100),
    city              VARCHAR(100),
    street            VARCHAR(255),
    coordinates       POINT,
    total_area        DOUBLE PRECISION CHECK (total_area > 0),
    living_area       DOUBLE PRECISION CHECK (living_area >= 0),
    balcony_area      DOUBLE PRECISION CHECK (balcony_area >= 0),
    description_ka    TEXT,
    description_en    TEXT,
    description_ru    TEXT,
    status            VARCHAR(50) NOT NULL
);


CREATE TABLE property_amenity
(
    property UUID         NOT NULL REFERENCES property (id) ON DELETE CASCADE,
    name     VARCHAR(255) NOT NULL,
    PRIMARY KEY (property, name)
);


CREATE TABLE IF NOT EXISTS event_publication
(
    id               UUID                     NOT NULL,
    listener_id      TEXT                     NOT NULL,
    event_type       TEXT                     NOT NULL,
    serialized_event TEXT                     NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date  TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS event_publication_serialized_event_hash_idx ON event_publication USING hash (serialized_event);
CREATE INDEX IF NOT EXISTS event_publication_by_completion_date_idx ON event_publication (completion_date);

CREATE TABLE photo_gallery
(
    id                UUID PRIMARY KEY,
    version           INT         NOT NULL,
    property_id       UUID        NOT NULL UNIQUE REFERENCES property (id) ON DELETE CASCADE,
    property_type     VARCHAR(50) NOT NULL,
    grouped_by_spaces BOOLEAN
);

CREATE TABLE photo
(
    uri               VARCHAR(255) PRIMARY KEY,
    space_id          UUID,
    space_order       INT,
    photo_gallery     UUID   NOT NULL REFERENCES photo_gallery (id) ON DELETE CASCADE,
    photo_gallery_key SERIAL NOT NULL,
    type              VARCHAR(50),
    tags              JSONB
);

CREATE TABLE space
(
    id             UUID PRIMARY KEY,
    version        INT          NOT NULL DEFAULT 0,
    property_id    UUID         NOT NULL REFERENCES property (id) ON DELETE CASCADE,
    type           VARCHAR(255) NOT NULL,
    name           VARCHAR(255) NOT NULL,
    description_ka TEXT,
    description_en TEXT,
    description_ru TEXT,
    area           DOUBLE PRECISION
);

CREATE TABLE space_amenity
(
    space UUID         NOT NULL REFERENCES space (id) ON DELETE CASCADE,
    name  VARCHAR(255) NOT NULL,
    PRIMARY KEY (space, name)
);