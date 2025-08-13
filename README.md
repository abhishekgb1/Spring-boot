# Currency Converter - Spring Boot

A real-time currency converter application built with Spring Boot backend and HTML/CSS frontend, integrated with ExchangeRate API.

## Features

- 🌍 **Real-time Exchange Rates** - Live data from ExchangeRate API
- 💱 **Currency Conversion** - Convert between 9 major currencies
- 📊 **Exchange Rates Table** - View current rates for popular currencies
- 🎨 **Modern UI** - Clean, responsive design with CSS animations
- 🔄 **Auto-refresh** - Rates update automatically every minute

## Supported Currencies

- 🇺🇸 USD - US Dollar
- 🇪🇺 EUR - Euro
- 🇬🇧 GBP - British Pound
- 🇮🇳 INR - Indian Rupee
- 🇯🇵 JPY - Japanese Yen
- 🇨🇦 CAD - Canadian Dollar
- 🇦🇺 AUD - Australian Dollar
- 🇨🇭 CHF - Swiss Franc
- 🇨🇳 CNY - Chinese Yuan

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- IntelliJ IDEA
- ExchangeRate API key (free at https://www.exchangerate-api.com/)

## Setup Instructions for IntelliJ IDEA

### 1. Import Project
1. Open IntelliJ IDEA
2. Click **File → Open** or **Import Project**
3. Select the project folder containing `pom.xml`
4. Choose **Import as Maven Project**
5. Wait for Maven to download dependencies

### 2. Configure API Key
1. Get your free API key from https://www.exchangerate-api.com/
2. In IntelliJ, go to **Run → Edit Configurations**
3. Create new **Spring Boot** configuration
4. Set **Main class**: `com.currencyconverter.CurrencyConverterApplication`
5. In **Environment Variables**, add:
   ```
   EXCHANGERATE_API_KEY=your_actual_api_key_here
   ```

### 3. Run the Application
1. Click the green **Run** button or press `Ctrl+Shift+F10`
2. Wait for the application to start (you'll see "Started CurrencyConverterApplication")
3. Open your browser and go to: **http://localhost:8080**

## Project Structure

```
src/
├── main/
│   ├── java/com/currencyconverter/
│   │   ├── CurrencyConverterApplication.java
│   │   ├── controller/
│   │   │   └── CurrencyController.java
│   │   ├── service/
│   │   │   └── ExchangeRateService.java
│   │   └── model/
│   │       ├── Currency.java
│   │       ├── ConversionRequest.java
│   │       └── ConversionResult.java
│   └── resources/
│       ├── static/
│       │   ├── css/style.css
│       │   └── js/app.js
│       ├── templates/
│       │   └── index.html
│       └── application.properties
└── pom.xml
```

## API Endpoints

- `GET /` - Main application page
- `POST /api/convert` - Convert currency
- `GET /api/currencies` - Get supported currencies
- `GET /api/rates/{baseCurrency}` - Get exchange rates

## Usage

1. **Convert Currency**: Enter amount, select currencies, click "Convert"
2. **Swap Currencies**: Click the swap button (⇄) between currency dropdowns
3. **View Rates**: Check the exchange rates table below the converter
4. **Refresh Rates**: Click the refresh button to get latest rates

## Troubleshooting

### Common Issues:

1. **API Key Error**: Make sure you've set the `EXCHANGERATE_API_KEY` environment variable
2. **Port Already in Use**: Change port in `application.properties`: `server.port=8081`
3. **Maven Dependencies**: Right-click project → Maven → Reload project

### IntelliJ Specific:

1. **Java Version**: File → Project Structure → Project → Set Project SDK to Java 17+
2. **Maven Import**: Enable auto-import in Settings → Build → Build Tools → Maven
3. **Run Configuration**: Make sure Spring Boot configuration is properly set up

## Development

- **Hot Reload**: Use Spring Boot DevTools (included) for automatic restart on changes
- **Debug Mode**: Run in debug mode to set breakpoints
- **Logs**: Check console for detailed API call logs and error messages

## Technologies Used

- **Backend**: Spring Boot 3.2.0, Java 17
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **Template Engine**: Thymeleaf
- **API**: ExchangeRate API v6
- **Build Tool**: Maven

---

🚀 **Ready to run!** Open in IntelliJ, set your API key, and start converting currencies with real-time rates!