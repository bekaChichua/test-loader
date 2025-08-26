-- 2. Create the 'property' table
CREATE TABLE property
(
    id                BIGSERIAL PRIMARY KEY,
    type              text           NOT NULL,
    listing_type      text           NOT NULL,
    price             INTEGER        NOT NULL,
    title             VARCHAR(500)   NOT NULL,
    total_area        NUMERIC(10, 2) NOT NULL,
    living_area       NUMERIC(10, 2),
    balcony_area      NUMERIC(10, 2),
    lat               NUMERIC(9, 6)  NOT NULL,
    lng               NUMERIC(9, 6)  NOT NULL,
    full_address_name TEXT           NOT NULL,
    country           VARCHAR(100)   NOT NULL DEFAULT 'Georgia',
    city              VARCHAR(255)   NOT NULL,
    street            VARCHAR(500)
);

-- Add indexes for frequently queried columns
CREATE INDEX idx_property_type ON property (type);
CREATE INDEX idx_property_listing_type ON property (listing_type);
CREATE INDEX idx_property_city ON property (city);
CREATE INDEX idx_property_price ON property (price);


-- 3. Create the 'photo' table
-- This table stores URLs of photos associated with a property.
CREATE TABLE photo
(
    property     BIGINT NOT NULL, -- Foreign key referencing the property
    url          TEXT   NOT NULL, -- URL of the photo
    property_key SERIAL NOT NULL  -- If a property is deleted, its photos are also deleted
);


-- 4. Create the 'space' table
-- This table stores different spaces within a property (e.g., rooms).
CREATE TABLE space
(
    property     BIGINT        NOT NULL,
    type         text          NOT NULL,
    area         NUMERIC(8, 2) NOT NULL, -- Area of the specific space
    property_key SERIAL        NOT NULL
);
