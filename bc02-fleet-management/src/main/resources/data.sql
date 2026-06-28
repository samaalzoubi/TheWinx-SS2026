INSERT INTO vehicles (
    provider_id,
    type,
    description,
    status,
    current_latitude,
    current_longitude,
    price_per_unit,
    billing_model,
    max_duration_minutes,
    max_kilometers,
    min_age,
    max_persons
)
VALUES
(1, 'E_SCOOTER', 'Tier E-Scooter - Dortmund City', 'AVAILABLE', 51.5136, 7.4653, 0.25, 'PER_KILOMETER', 120, 30, 18, 1),
(1, 'BICYCLE', 'Nextbike City Bicycle', 'AVAILABLE', 51.5142, 7.4628, 2.00, 'PER_HOUR', 240, 50, 16, 1),
(1, 'E_BIKE', 'Lime E-Bike', 'BOOKED', 51.5150, 7.4680, 3.50, 'PER_HOUR', 180, 60, 18, 1),
(2, 'E_CAR', 'ShareNow Electric Car', 'AVAILABLE', 51.5105, 7.4701, 12.00, 'PER_HOUR', 480, 250, 21, 4),
(2, 'E_SCOOTER', 'Voi E-Scooter', 'AVAILABLE', 51.5161, 7.4599, 0.30, 'PER_KILOMETER', 120, 25, 18, 1),
(2, 'BICYCLE', 'Swapfiets Bicycle', 'BOOKED', 51.5180, 7.4665, 1.50, 'PER_HOUR', 300, 70, 16, 1),
(3, 'E_BIKE', 'Bolt E-Bike', 'AVAILABLE', 51.5122, 7.4720, 3.00, 'PER_HOUR', 180, 50, 18, 1),
(3, 'E_CAR', 'Sixt Electric Car', 'AVAILABLE', 51.5098, 7.4612, 15.00, 'PER_HOUR', 720, 300, 21, 5);