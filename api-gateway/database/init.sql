-- HRS Hotel Booking System Database Initialization
-- Created: 2025-06-27 11:31:36 UTC
-- Author: arihants1

-- Set timezone and encoding
SET timezone = 'UTC';
SET client_encoding = 'UTF8';

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create service users with proper permissions
DO $$
    BEGIN
        -- Create users if they don't exist
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'hotel_user') THEN
            CREATE USER hotel_user WITH PASSWORD 'hotel_password_2025';
        END IF;

        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'booking_user') THEN
            CREATE USER booking_user WITH PASSWORD 'booking_password_2025';
        END IF;

        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'user_user') THEN
            CREATE USER user_user WITH PASSWORD 'user_password_2025';
        END IF;
    END
$$;

-- Grant database privileges
GRANT ALL PRIVILEGES ON DATABASE hotel_booking TO hotel_user;
GRANT ALL PRIVILEGES ON DATABASE hotel_booking TO booking_user;
GRANT ALL PRIVILEGES ON DATABASE hotel_booking TO user_user;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO hotel_user;
GRANT ALL ON SCHEMA public TO booking_user;
GRANT ALL ON SCHEMA public TO user_user;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO hotel_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO booking_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO user_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO hotel_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO booking_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO user_user;

-- Create hotels table
CREATE TABLE IF NOT EXISTS hotels (
                                      id BIGSERIAL PRIMARY KEY,
                                      name VARCHAR(255) NOT NULL,
                                      description TEXT,
                                      location VARCHAR(500) NOT NULL,
                                      city VARCHAR(100) NOT NULL,
                                      country VARCHAR(100) NOT NULL,
                                      star_rating INTEGER CHECK (star_rating >= 1 AND star_rating <= 5),
                                      amenities JSONB,
                                      base_price DECIMAL(10,2) CHECK (base_price > 0),
                                      total_rooms INTEGER CHECK (total_rooms > 0),
                                      is_active BOOLEAN DEFAULT true,
                                      phone VARCHAR(20),
                                      email VARCHAR(100),
                                      website VARCHAR(255),
                                      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                      created_by VARCHAR(50) DEFAULT 'arihants1',
                                      updated_by VARCHAR(50) DEFAULT 'arihants1'
);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     first_name VARCHAR(100) NOT NULL,
                                     last_name VARCHAR(100) NOT NULL,
                                     email VARCHAR(255) NOT NULL UNIQUE,
                                     phone VARCHAR(20),
                                     is_active BOOLEAN DEFAULT true,
                                     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                     created_by VARCHAR(50) DEFAULT 'arihants1',
                                     updated_by VARCHAR(50) DEFAULT 'arihants1'
);

-- Create bookings table
CREATE TABLE IF NOT EXISTS bookings (
                                        id BIGSERIAL PRIMARY KEY,
                                        user_id BIGINT NOT NULL,
                                        hotel_id BIGINT NOT NULL,
                                        check_in_date DATE NOT NULL,
                                        check_out_date DATE NOT NULL,
                                        room_type VARCHAR(100),
                                        number_of_rooms INTEGER NOT NULL DEFAULT 1 CHECK (number_of_rooms > 0),
                                        number_of_guests INTEGER NOT NULL DEFAULT 1 CHECK (number_of_guests > 0),
                                        total_amount DECIMAL(12,2) CHECK (total_amount >= 0),
                                        base_amount DECIMAL(12,2) CHECK (base_amount >= 0),
                                        taxes_amount DECIMAL(12,2) DEFAULT 0 CHECK (taxes_amount >= 0),
                                        fees_amount DECIMAL(12,2) DEFAULT 0 CHECK (fees_amount >= 0),
                                        status VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED',
                                        booking_reference VARCHAR(50) NOT NULL UNIQUE,
                                        confirmation_number VARCHAR(50) UNIQUE,
                                        special_requests TEXT,
                                        guest_name VARCHAR(255),
                                        guest_email VARCHAR(255),
                                        guest_phone VARCHAR(50),
                                        payment_status VARCHAR(50) DEFAULT 'PENDING',
                                        payment_method VARCHAR(50),
                                        payment_reference VARCHAR(100),
                                        cancellation_reason TEXT,
                                        cancelled_at TIMESTAMP WITH TIME ZONE,
                                        cancelled_by VARCHAR(50),
                                        checked_in_at TIMESTAMP WITH TIME ZONE,
                                        checked_out_at TIMESTAMP WITH TIME ZONE,
                                        discount_amount DECIMAL(10,2) DEFAULT 0 CHECK (discount_amount >= 0),
                                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                        created_by VARCHAR(50) DEFAULT 'arihants1',
                                        updated_by VARCHAR(50) DEFAULT 'arihants1',
                                        version BIGINT DEFAULT 0
);

-- Create performance indexes for hotels
CREATE INDEX IF NOT EXISTS idx_hotel_location ON hotels(city, country) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_hotel_price ON hotels(base_price) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_hotel_rating ON hotels(star_rating) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_hotel_active ON hotels(is_active);
CREATE INDEX IF NOT EXISTS idx_hotel_name ON hotels(name) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_hotels_amenities ON hotels USING GIN(amenities) WHERE is_active = true;
CREATE UNIQUE INDEX IF NOT EXISTS idx_hotels_unique_name_city ON hotels(LOWER(name), LOWER(city)) WHERE is_active = true;

-- Create performance indexes for users
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_user_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_user_name ON users(first_name, last_name) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_user_phone ON users(phone) WHERE phone IS NOT NULL AND is_active = true;

-- Create performance indexes for bookings
CREATE INDEX IF NOT EXISTS idx_booking_user ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_booking_hotel ON bookings(hotel_id);
CREATE INDEX IF NOT EXISTS idx_booking_dates ON bookings(check_in_date, check_out_date);
CREATE INDEX IF NOT EXISTS idx_booking_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_booking_reference ON bookings(booking_reference);
CREATE INDEX IF NOT EXISTS idx_booking_confirmation ON bookings(confirmation_number);
CREATE INDEX IF NOT EXISTS idx_booking_user_status ON bookings(user_id, status);
CREATE INDEX IF NOT EXISTS idx_booking_hotel_dates ON bookings(hotel_id, check_in_date, check_out_date);

-- Create trigger functions for automatic timestamp updates
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    NEW.updated_by = 'arihants1';
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for all tables
CREATE TRIGGER trigger_hotels_updated_at
    BEFORE UPDATE ON hotels
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_bookings_updated_at
    BEFORE UPDATE ON bookings
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data for hotels
INSERT INTO hotels (name, description, location, city, country, star_rating, amenities, base_price, total_rooms, phone, email, website) VALUES
                                                                                                                                            ('HRS Grand Hotel New York', 'Luxury hotel in the heart of Manhattan with stunning city views and world-class amenities', '123 Broadway, Manhattan', 'New York', 'USA', 5, '{"wifi": true, "pool": true, "gym": true, "spa": true, "restaurant": true, "room_service": true, "concierge": true, "parking": true, "business_center": true}', 299.99, 200, '+1-212-555-0101', 'info@hrsgrandny.com', 'https://hrsgrandny.com'),

                                                                                                                                            ('HRS Business Center London', 'Modern business hotel near financial district with state-of-the-art conference facilities', '456 King Street, City of London', 'London', 'UK', 4, '{"wifi": true, "business_center": true, "gym": true, "restaurant": true, "room_service": true, "conference_rooms": true, "parking": false, "airport_shuttle": true}', 189.99, 150, '+44-20-7555-0201', 'reservations@hrslondon.com', 'https://hrslondon.com'),

                                                                                                                                            ('HRS Resort Miami Beach', 'Beachfront resort with ocean views and tropical amenities for the perfect vacation', '789 Ocean Drive, South Beach', 'Miami', 'USA', 4, '{"wifi": true, "pool": true, "beach_access": true, "gym": true, "spa": true, "restaurant": true, "bar": true, "room_service": true, "water_sports": true, "tennis": true}', 249.99, 180, '+1-305-555-0301', 'bookings@hrsmiami.com', 'https://hrsmiami.com'),

                                                                                                                                            ('HRS City Express Berlin', 'Affordable hotel with modern amenities in the heart of Berlin', '321 Unter den Linden, Mitte', 'Berlin', 'Germany', 3, '{"wifi": true, "breakfast": true, "gym": false, "restaurant": true, "parking": true, "bike_rental": true, "24h_reception": true}', 89.99, 120, '+49-30-555-0401', 'stay@hrsberlin.de', 'https://hrsberlin.de'),

                                                                                                                                            ('HRS Boutique Tokyo', 'Contemporary boutique hotel in trendy Shibuya district', '654 Shibuya Crossing, Shibuya', 'Tokyo', 'Japan', 4, '{"wifi": true, "restaurant": true, "bar": true, "gym": true, "spa": true, "room_service": true, "concierge": true, "cultural_tours": true}', 199.99, 100, '+81-3-555-0501', 'hello@hrstokyo.jp', 'https://hrstokyo.jp');

-- Insert sample data for users
INSERT INTO users (first_name, last_name, email, phone) VALUES
                                                            ('John', 'Doe', 'john.doe@example.com', '+1-555-0123'),
                                                            ('Jane', 'Smith', 'jane.smith@example.com', '+44-20-555-0124'),
                                                            ('Admin', 'User', 'admin@hrs.com', '+1-555-0100'),
                                                            ('Alice', 'Johnson', 'alice.johnson@example.com', '+1-555-0125'),
                                                            ('Bob', 'Wilson', 'bob.wilson@example.com', '+49-30-555-0126'),
                                                            ('Charlie', 'Brown', 'charlie.brown@example.com', '+81-3-555-0127'),
                                                            ('Diana', 'Martinez', 'diana.martinez@example.com', '+1-305-555-0128'),
                                                            ('Eva', 'Garcia', 'eva.garcia@example.com', '+34-91-555-0129');

-- Insert sample bookings
INSERT INTO bookings (user_id, hotel_id, check_in_date, check_out_date, room_type, number_of_rooms, number_of_guests,
                      total_amount, base_amount, taxes_amount, fees_amount, booking_reference, confirmation_number,
                      guest_name, guest_email, guest_phone, payment_status) VALUES
                                                                                (1, 1, '2025-07-15', '2025-07-18', 'DELUXE', 1, 2, 989.97, 899.97, 89.97, 45.00, 'HRS_20250627113136_0001', 'CONF20250627000001', 'John Doe', 'john.doe@example.com', '+1-555-0123', 'COMPLETED'),
                                                                                (2, 2, '2025-07-20', '2025-07-22', 'STANDARD', 1, 1, 417.98, 379.98, 37.98, 19.00, 'HRS_20250627113136_0002', 'CONF20250627000002', 'Jane Smith', 'jane.smith@example.com', '+44-20-555-0124', 'PENDING'),
                                                                                (1, 3, '2025-08-01', '2025-08-05', 'SUITE', 2, 4, 1099.96, 999.96, 99.96, 50.00, 'HRS_20250627113136_0003', 'CONF20250627000003', 'John Doe', 'john.doe@example.com', '+1-555-0123', 'CONFIRMED'),
                                                                                (3, 4, '2025-07-25', '2025-07-27', 'STANDARD', 1, 1, 197.98, 179.98, 17.98, 9.00, 'HRS_20250627113136_0004', 'CONF20250627000004', 'Admin User', 'admin@hrs.com', '+1-555-0100', 'CONFIRMED'),
                                                                                (4, 5, '2025-08-10', '2025-08-13', 'DELUXE', 1, 2, 659.97, 599.97, 59.97, 30.00, 'HRS_20250627113136_0005', 'CONF20250627000005', 'Alice Johnson', 'alice.johnson@example.com', '+1-555-0125', 'PENDING');

-- Add table comments
COMMENT ON TABLE hotels IS 'HRS Hotels table - stores hotel information. Created: 2025-06-27 11:31:36 UTC by arihants1';
COMMENT ON TABLE users IS 'HRS Users table - stores user profiles. Created: 2025-06-27 11:31:36 UTC by arihants1';
COMMENT ON TABLE bookings IS 'HRS Bookings table - stores booking information. Created: 2025-06-27 11:31:36 UTC by arihants1';

-- Display initialization summary
DO $$
    DECLARE
        hotel_count INTEGER;
        user_count INTEGER;
        booking_count INTEGER;
    BEGIN
        SELECT COUNT(*) INTO hotel_count FROM hotels;
        SELECT COUNT(*) INTO user_count FROM users;
        SELECT COUNT(*) INTO booking_count FROM bookings;

        RAISE NOTICE '=== HRS Database Initialization Complete ===';
        RAISE NOTICE 'Timestamp: 2025-06-27 11:31:36 UTC';
        RAISE NOTICE 'Initialized by: arihants1';
        RAISE NOTICE 'Hotels created: %', hotel_count;
        RAISE NOTICE 'Users created: %', user_count;
        RAISE NOTICE 'Bookings created: %', booking_count;
        RAISE NOTICE '===========================================';
    END $$;