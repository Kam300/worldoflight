# World of Light(Мир Света) - Android приложение для продажи светотехники

Современное Android приложение для интернет-магазина светотехники с полным функционалом электронной коммерции.

## 📱 Описание

World of Light(мир света) - это мобильное приложение для продажи осветительного оборудования, включающее в себя:
- Каталог товаров с категориями
- Систему аутентификации пользователей
- Корзину покупок
- Акции с промокодами
- Профиль пользователя
- Управление остатками товаров fix 3

## ✨ Функциональность

### 🔐 Аутентификация
- Регистрация с подтверждением по email (OTP код)
- Вход в систему
- Восстановление пароля
- Безопасное хранение данных с шифрованием

### 🛍️ Каталог товаров
- Просмотр категорий: лампочки, люстры, торшеры, настольные лампы, бра, LED ленты
- Популярные товары
- Поиск по товарам, нету еще
- Детальная информация о товарах
- Отображение остатков на складе

### 👤 Профиль пользователя
- Редактирование личных данных
- Загрузка аватара
- QR-код профиля статичный
- Безопасное хранение данных

### 🛒 Корзина покупок
- Добавление товаров в корзину
- Управление количеством
- Ввод промокоды для скидки
- Оформление заказов, не реализованно проверка количества

## 🛠️ Технологии

### Frontend (Android)
- **Kotlin** - основной язык разработки
- **Android Architecture Components** - MVVM архитектура
- **View Binding** - связывание представлений
- **RecyclerView** - списки товаров
- **Material Design 3** - современный дизайн
- **Glide** - загрузка изображений
- **EncryptedSharedPreferences** - безопасное хранение данных

### Backend
- **Supabase** - Backend-as-a-Service
- **PostgreSQL** - база данных
- **Row Level Security (RLS)** - безопасность данных
- **Supabase Auth** - аутентификация
- **Supabase Storage** - хранение файлов

### Архитектура
- **MVVM** (Model-View-ViewModel)
- **Repository Pattern** - слой данных
- **LiveData** - реактивное программирование
- **Coroutines** - асинхронное программирование

## 📦 Установка и запуск

### Предварительные требования
- Android Studio Arctic Fox или новее
- Android SDK 24+
- Аккаунт Supabase

### Настройка проекта

1. **Клонирование репозитория**
```bash
git clone https://github.com/Kam300/worldoflight.git
cd worldoflight
```

2. **Настройка Supabase**
- Создайте проект в [Supabase](https://supabase.com)
- Скопируйте URL проекта и anon key
- Обновите `SupabaseClient.kt`:
```kotlin
val client = createSupabaseClient(
    supabaseUrl = "YOUR_SUPABASE_URL",
    supabaseKey = "YOUR_SUPABASE_ANON_KEY"
)
```

3. **Создание таблиц в базе данных**
Выполните SQL скрипты в Supabase SQL Editor:

```sql
-- Создание таблицы профилей
CREATE TABLE profiles (
    id UUID REFERENCES auth.users(id) PRIMARY KEY,
    email TEXT NOT NULL,
    name TEXT,
    surname TEXT,
    phone TEXT,
    address TEXT,
    avatar_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Создание таблицы категорий
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    display_name TEXT NOT NULL,
    description TEXT,
    icon_name TEXT,
    is_active BOOLEAN DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Создание таблицы продуктов
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category TEXT NOT NULL,
    image_url TEXT,
    stock_quantity INTEGER DEFAULT 0,
    in_stock BOOLEAN DEFAULT true,
    brand TEXT,
    power TEXT,
    color_temperature TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

—— таблица для акции (промокод)
create table public.promotions (
  id bigserial not null,
  title text not null,
  description text null,
  promo_code text not null,
  discount_type text not null,
  discount_value numeric(10, 2) not null,
  min_order_amount numeric(10, 2) null default 0,
  max_discount_amount numeric(10, 2) null,
  image_url text null,
  start_date timestamp with time zone not null,
  end_date timestamp with time zone not null,
  usage_limit integer null,
  used_count integer null default 0,
  is_active boolean null default true,
  created_at timestamp with time zone null default now(),
  updated_at timestamp with time zone null default now(),
  constraint promotions_pkey primary key (id),
  constraint promotions_promo_code_key unique (promo_code),
  constraint promotions_discount_type_check check (
    (
      discount_type = any (array['percentage'::text, 'fixed_amount'::text])
    )
  )
) TABLESPACE pg_default;

create index IF not exists idx_promotions_promo_code on public.promotions using btree (promo_code) TABLESPACE pg_default;

create index IF not exists idx_promotions_active on public.promotions using btree (is_active, start_date, end_date) TABLESPACE pg_default;




-- Создание таблицы заказов
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id),
    order_number TEXT UNIQUE NOT NULL,
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'confirmed', 'shipped', 'delivered', 'cancelled')),
    total_amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    delivery_fee DECIMAL(10,2) DEFAULT 0,
    payment_method TEXT NOT NULL,
    delivery_address TEXT NOT NULL,
    contact_phone TEXT NOT NULL,
    contact_email TEXT NOT NULL,
    promo_code TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    estimated_delivery TIMESTAMP WITH TIME ZONE
);

-- Создание таблицы элементов заказа
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id),
    product_name TEXT NOT NULL,
    product_price DECIMAL(10,2) NOT NULL,
    quantity INTEGER NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Создание индексов
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- Удаляем старый триггер если есть DROP TRIGGER IF EXISTS trigger_update_stock ON order_items; DROP FUNCTION IF EXISTS update_product_stock();

-- Создаем новую функцию с логированием CREATE OR REPLACE FUNCTION update_product_stock() RETURNS TRIGGER AS $$ DECLARE current_stock INTEGER; product_name_var TEXT; BEGIN -- Получаем текущие остатки и название товара SELECT stock_quantity, name INTO current_stock, product_name_var FROM products WHERE id = NEW.product_id;


-- Логируем начальное состояние
CREATE TABLE orders ( id BIGSERIAL PRIMARY KEY, user_id UUID REFERENCES auth.users(id), order_number TEXT UNIQUE NOT NULL, status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'confirmed', 'shipped', 'delivered', 'cancelled')), total_amount DECIMAL(10,2) NOT NULL, discount_amount DECIMAL(10,2) DEFAULT 0, delivery_fee DECIMAL(10,2) DEFAULT 0, payment_method TEXT NOT NULL, delivery_address TEXT NOT NULL, contact_phone TEXT NOT NULL, contact_email TEXT NOT NULL, promo_code TEXT, created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(), estimated_delivery TIMESTAMP WITH TIME ZONE );

-- Создание таблицы элементов заказа CREATE TABLE order_items ( id BIGSERIAL PRIMARY KEY, order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE, product_id BIGINT REFERENCES products(id), product_name TEXT NOT NULL, product_price DECIMAL(10,2) NOT NULL, quantity INTEGER NOT NULL, total_price DECIMAL(10,2) NOT NULL, created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() );

-- Создание индексов CREATE INDEX idx_orders_user_id ON orders(user_id); CREATE INDEX idx_orders_status ON orders(status); CREATE INDEX idx_order_items_order_id ON order_items(order_id);-- Создание таблицы профилей

```

4. **Настройка Storage**
- Создайте bucket "avatars" в Supabase Storage
- Настройте политики доступа

5. **Сборка проекта**
```bash
./gradlew assembleDebug
```

## 📁 Структура проекта

```
app/
├── src/main/java/com/worldoflight/
│   ├── data/
│   │   ├── models/          # Модели данных
│   │   ├── repositories/    # Репозитории
│   │   └── remote/          # Supabase клиент
│   ├── ui/
│   │   ├── activities/      # Активности
│   │   ├── fragments/       # Фрагменты
│   │   ├── adapters/        # Адаптеры для списков
│   │   └── viewmodels/      # ViewModel классы
│   └── utils/               # Утилиты
├── src/main/res/
│   ├── layout/              # XML макеты
│   ├── drawable/            # Иконки и изображения
│   ├── values/              # Цвета, строки, стили
│   └── font/                # Шрифты
```

## 🎨 Дизайн

Приложение использует Material Design 3 с кастомной цветовой схемой:
- **Основной цвет**: Светло-голубой (#60A5FA)
- **Акцентный цвет**: Золотой (#FFD700)
- **Фон**: Темно-серый (#1A1A1A)
- **Шрифт**: Raleway

## 🔒 Безопасность

- Шифрование локальных данных с EncryptedSharedPreferences
- Row Level Security (RLS) в Supabase
- Безопасная аутентификация с JWT токенами
- Валидация данных на клиенте и сервере

## 📱 Поддерживаемые версии Android

- **Минимальная версия**: Android 7.0 (API 24)
- **Целевая версия**: Android 14 (API 34)

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте ветку для новой функции (`git checkout -b feature/AmazingFeature`)
3. Зафиксируйте изменения (`git commit -m 'Add some AmazingFeature'`)
4. Отправьте в ветку (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request

## 📄 Лицензия

Этот проект распространяется под лицензией MIT. См. файл `LICENSE` для подробностей.

## 👨‍💻 Автор

**Kamil** - [Kam300](https://github.com/Kam300)

## 📞 Контакты

- Email: kamilgaripov205@gmail.com
- GitHub: [@Kam300](https://github.com/Kam300)

## 🙏 Благодарности

- [Supabase](https://supabase.com) - за отличный Backend-as-a-Service
- [Material Design](https://material.io) - за дизайн-систему
- [Glide](https://github.com/bumptech/glide) - за библиотеку загрузки изображений

---

⭐ Поставьте звезду, если проект был полезен!
