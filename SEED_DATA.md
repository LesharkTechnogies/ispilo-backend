# Database Seed Data

Run the following SQL scripts in your PostgreSQL database to populate it with initial test data. This will ensure your mobile app has content to display immediately after connection.

## 1. Users
*Password for all users is `password123` (hashed with BCrypt).*

```sql
INSERT INTO users (id, email, password_hash, first_name, last_name, name, phone, country_code, county, town, is_verified, is_email_verified, is_phone_verified, bio, avatar, created_at, updated_at) VALUES
('u1', 'john.doe@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt8AEuldrwH3q8F.d7rsB.w.ywa', 'John', 'Doe', 'John Doe', '+254712345678', 'KE', 'Nairobi', 'Westlands', true, true, true, 'Tech enthusiast and ISP engineer.', 'https://i.pravatar.cc/300?img=11', NOW(), NOW()),
('u2', 'jane.smith@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt8AEuldrwH3q8F.d7rsB.w.ywa', 'Jane', 'Smith', 'Jane Smith', '+254722345678', 'KE', 'Mombasa', 'Nyali', true, true, true, 'Digital nomad living the dream.', 'https://i.pravatar.cc/300?img=5', NOW(), NOW()),
('u3', 'michael.brown@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt8AEuldrwH3q8F.d7rsB.w.ywa', 'Michael', 'Brown', 'Michael Brown', '+254732345678', 'KE', 'Kisumu', 'Milimani', false, true, false, 'Love photography and travel.', 'https://i.pravatar.cc/300?img=3', NOW(), NOW()),
('u4', 'sarah.wilson@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt8AEuldrwH3q8F.d7rsB.w.ywa', 'Sarah', 'Wilson', 'Sarah Wilson', '+254742345678', 'KE', 'Nakuru', 'Njoro', true, true, true, 'Fashion designer and content creator.', 'https://i.pravatar.cc/300?img=9', NOW(), NOW()),
('u5', 'david.lee@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt8AEuldrwH3q8F.d7rsB.w.ywa', 'David', 'Lee', 'David Lee', '+254752345678', 'KE', 'Nairobi', 'Kilimani', false, false, false, 'Gamer and developer.', 'https://i.pravatar.cc/300?img=13', NOW(), NOW()),
('u6', 'emily.davis@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt8AEuldrwH3q8F.d7rsB.w.ywa', 'Emily', 'Davis', 'Emily Davis', '+254762345678', 'KE', 'Eldoret', 'Pioneer', true, true, true, 'Fitness coach and nutritionist.', 'https://i.pravatar.cc/300?img=20', NOW(), NOW()),
('u7', 'chris.martin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt8AEuldrwH3q8F.d7rsB.w.ywa', 'Chris', 'Martin', 'Chris Martin', '+254772345678', 'KE', 'Nairobi', 'Karen', true, true, true, 'Musician and producer.', 'https://i.pravatar.cc/300?img=15', NOW(), NOW()),
('u8', 'lisa.taylor@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt8AEuldrwH3q8F.d7rsB.w.ywa', 'Lisa', 'Taylor', 'Lisa Taylor', '+254782345678', 'KE', 'Thika', 'Section 9', false, true, false, 'Student at MKU.', 'https://i.pravatar.cc/300?img=25', NOW(), NOW()),
('u9', 'james.anderson@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt8AEuldrwH3q8F.d7rsB.w.ywa', 'James', 'Anderson', 'James Anderson', '+254792345678', 'KE', 'Nairobi', 'Roysambu', true, true, true, 'Freelance graphic designer.', 'https://i.pravatar.cc/300?img=60', NOW(), NOW()),
('u10', 'patricia.thomas@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt8AEuldrwH3q8F.d7rsB.w.ywa', 'Patricia', 'Thomas', 'Patricia Thomas', '+254702345678', 'KE', 'Machakos', 'Town', true, true, true, 'Small business owner.', 'https://i.pravatar.cc/300?img=45', NOW(), NOW());
```

## 2. Sellers (Marketplace Profiles)
*Users u2, u4, u6, u7, and u10 are sellers.*

```sql
INSERT INTO sellers (id, user_id, business_name, business_description, business_address, is_verified, created_at, updated_at) VALUES
('s1', 'u2', 'Jane''s Gadgets', 'Best electronics in Mombasa.', 'Nyali Mall, Shop 4', true, NOW(), NOW()),
('s2', 'u4', 'Sarah''s Boutique', 'Trendy fashion for everyone.', 'Westside Mall, Nakuru', true, NOW(), NOW()),
('s3', 'u6', 'FitLife Supplements', 'High quality supplements and gym gear.', 'Zion Mall, Eldoret', true, NOW(), NOW()),
('s4', 'u7', 'Karen Music Store', 'Instruments and audio equipment.', 'Karen Shopping Centre', true, NOW(), NOW()),
('s5', 'u10', 'Machakos Crafts', 'Handmade baskets and decor.', 'Machakos Town Center', false, NOW(), NOW());
```

## 3. Posts
*Social feed content.*

```sql
INSERT INTO posts (id, user_id, description, likes_count, comments_count, view_count, created_at, updated_at) VALUES
('p1', 'u1', 'Just finished setting up the new server rack! 🖥️ #ISP #Networking', 120, 5, 450, NOW() - INTERVAL '2 HOURS', NOW()),
('p2', 'u2', 'Beautiful sunset at the beach today. 🌅', 340, 12, 1200, NOW() - INTERVAL '5 HOURS', NOW()),
('p3', 'u3', 'Anyone know where to get good fiber cables in Kisumu?', 45, 8, 200, NOW() - INTERVAL '1 DAY', NOW()),
('p4', 'u4', 'New collection dropping soon! Stay tuned. 👗', 210, 25, 890, NOW() - INTERVAL '3 DAYS', NOW()),
('p5', 'u1', 'Coding late into the night. ☕', 89, 2, 300, NOW() - INTERVAL '4 DAYS', NOW()),
('p6', 'u6', 'Morning run done! 10km in 50mins. 🏃‍♀️💨 #Fitness #Eldoret', 150, 20, 600, NOW() - INTERVAL '6 HOURS', NOW()),
('p7', 'u7', 'Jamming session with the band. 🎸🥁', 280, 15, 950, NOW() - INTERVAL '2 DAYS', NOW()),
('p8', 'u8', 'Exams are finally over! Time to relax. 🎉', 180, 30, 700, NOW() - INTERVAL '12 HOURS', NOW()),
('p9', 'u9', 'Working on a new logo design for a client. 🎨', 95, 10, 400, NOW() - INTERVAL '1 DAY', NOW()),
('p10', 'u10', 'Fresh batch of kiondos available! DM to order.', 60, 5, 250, NOW() - INTERVAL '5 DAYS', NOW()),
('p11', 'u2', 'Trying out the new seafood place in town. 🍤', 200, 18, 800, NOW() - INTERVAL '6 DAYS', NOW()),
('p12', 'u5', 'Just reached Diamond rank! 🎮🏆', 110, 40, 500, NOW() - INTERVAL '2 HOURS', NOW()),
('p13', 'u3', 'Lake Victoria looking calm this evening.', 130, 8, 450, NOW() - INTERVAL '3 DAYS', NOW()),
('p14', 'u4', 'Behind the scenes of our photoshoot. 📸', 250, 22, 1000, NOW() - INTERVAL '1 WEEK', NOW()),
('p15', 'u1', 'Upgrading the core router firmware. Fingers crossed! 🤞', 75, 12, 350, NOW() - INTERVAL '8 HOURS', NOW());
```

## 4. Post Media (Images/Videos)
*Linking images to posts.*

```sql
INSERT INTO post_media (post_id, media_url) VALUES
('p1', 'https://images.unsplash.com/photo-1558494949-efc5e60c9480?w=800&q=80'),
('p2', 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&q=80'),
('p4', 'https://images.unsplash.com/photo-1483985988355-763728e1935b?w=800&q=80'),
('p5', 'https://images.unsplash.com/photo-1587620962725-abab7fe55159?w=800&q=80'),
('p6', 'https://images.unsplash.com/photo-1502904550040-7534597429ae?w=800&q=80'),
('p7', 'https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=800&q=80'),
('p9', 'https://images.unsplash.com/photo-1626785774573-4b799312afc2?w=800&q=80'),
('p10', 'https://images.unsplash.com/photo-1605641288986-53c8d2b6c3a2?w=800&q=80'),
('p11', 'https://images.unsplash.com/photo-1559339352-11d035aa65de?w=800&q=80'),
('p13', 'https://images.unsplash.com/photo-1505245208761-ba872912fac0?w=800&q=80'),
('p14', 'https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=800&q=80');
```

## 5. Comments
*Interactions on posts.*

```sql
INSERT INTO comments (id, post_id, user_id, content, created_at) VALUES
('c1', 'p1', 'u3', 'Great job! Looks clean.', NOW() - INTERVAL '1 HOUR'),
('c2', 'p1', 'u5', 'Specs?', NOW() - INTERVAL '30 MINUTES'),
('c3', 'p2', 'u4', 'Wish I was there!', NOW() - INTERVAL '4 HOURS'),
('c4', 'p3', 'u1', 'Check out TechWorld in town, they usually have stock.', NOW() - INTERVAL '20 HOURS'),
('c5', 'p6', 'u2', 'Keep it up Emily!', NOW() - INTERVAL '5 HOURS'),
('c6', 'p6', 'u8', 'Goals! 💪', NOW() - INTERVAL '4 HOURS'),
('c7', 'p7', 'u9', 'Sounds amazing man.', NOW() - INTERVAL '1 DAY'),
('c8', 'p12', 'u1', 'GG WP', NOW() - INTERVAL '1 HOUR'),
('c9', 'p4', 'u6', 'Love that dress!', NOW() - INTERVAL '2 DAYS'),
('c10', 'p10', 'u2', 'How much for the large one?', NOW() - INTERVAL '4 DAYS');
```

## 6. Products
*Marketplace items.*

```sql
INSERT INTO products (id, seller_id, name, title, description, price, stock_quantity, category, is_available, created_at, updated_at) VALUES
('prod1', 's1', 'Wireless Router', 'Wireless Router', 'High speed dual-band router.', 4500.00, 10, 'Electronics', true, NOW(), NOW()),
('prod2', 's1', 'Ethernet Cable (10m)', 'Ethernet Cable (10m)', 'Cat6 high quality cable.', 500.00, 50, 'Electronics', true, NOW(), NOW()),
('prod3', 's2', 'Summer Dress', 'Summer Dress', 'Floral print summer dress.', 2500.00, 15, 'Fashion', true, NOW(), NOW()),
('prod4', 's2', 'Denim Jacket', 'Denim Jacket', 'Classic blue denim jacket.', 3000.00, 8, 'Fashion', true, NOW(), NOW()),
('prod5', 's3', 'Whey Protein', 'Whey Protein', 'Chocolate flavor, 2kg tub.', 6500.00, 20, 'Health & Beauty', true, NOW(), NOW()),
('prod6', 's3', 'Yoga Mat', 'Yoga Mat', 'Non-slip exercise mat.', 1500.00, 30, 'Sports', true, NOW(), NOW()),
('prod7', 's4', 'Acoustic Guitar', 'Acoustic Guitar', 'Yamaha F310, great for beginners.', 18000.00, 5, 'Hobbies', true, NOW(), NOW()),
('prod8', 's4', 'Microphone Stand', 'Microphone Stand', 'Adjustable boom arm stand.', 2500.00, 12, 'Hobbies', true, NOW(), NOW()),
('prod9', 's5', 'Sisal Basket (Large)', 'Sisal Basket (Large)', 'Handwoven kiondo, large size.', 1200.00, 25, 'Home & Living', true, NOW(), NOW()),
('prod10', 's5', 'Beaded Necklace', 'Beaded Necklace', 'Maasai beadwork necklace.', 800.00, 40, 'Fashion', true, NOW(), NOW()),
('prod11', 's1', 'Smart Watch', 'Smart Watch', 'Fitness tracker with heart rate monitor.', 3500.00, 15, 'Electronics', true, NOW(), NOW()),
('prod12', 's2', 'Leather Handbag', 'Leather Handbag', 'Genuine leather brown handbag.', 4500.00, 6, 'Fashion', true, NOW(), NOW());
```

## 7. Product Images
*Images for products.*

```sql
INSERT INTO product_images (product_id, image_url) VALUES
('prod1', 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?w=800&q=80'),
('prod2', 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?w=800&q=80'),
('prod3', 'https://images.unsplash.com/photo-1515347619252-60a6bf4fffce?w=800&q=80'),
('prod4', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&q=80'),
('prod5', 'https://images.unsplash.com/photo-1579722821273-0f6c7d44362f?w=800&q=80'),
('prod6', 'https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=800&q=80'),
('prod7', 'https://images.unsplash.com/photo-1550291652-6ea9114a47b1?w=800&q=80'),
('prod8', 'https://images.unsplash.com/photo-1520523839897-bd0b52f945a0?w=800&q=80'),
('prod9', 'https://images.unsplash.com/photo-1605641288986-53c8d2b6c3a2?w=800&q=80'),
('prod10', 'https://images.unsplash.com/photo-1611591437281-460bfbe1220a?w=800&q=80'),
('prod11', 'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=800&q=80'),
('prod12', 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=800&q=80');
```
