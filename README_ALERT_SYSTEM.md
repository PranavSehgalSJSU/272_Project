# Emergency Alert System

A complete emergency broadcast system integrated with existing multi-channel sender infrastructure, featuring rule-based alerting with scheduled evaluation and a web-based management dashboard.

## 🎯 Features

### Backend (Spring Boot + MongoDB)
- **Rule Management**: Create, edit, delete, and toggle alert rules
- **Multi-Source Support**: Weather API, status checks, and custom data sources
- **Conditional Logic**: Safe expression evaluation with dynamic conditions
- **Audience Targeting**: Tag-based and location-based recipient filtering
- **Message Templating**: Dynamic message generation with data placeholders
- **Multi-Channel Delivery**: Email, SMS, and extensible channel support
- **Scheduled Worker**: 5-minute interval rule evaluation and alert firing
- **Event Tracking**: Complete audit trail of fired alerts with results
- **Cooldown Management**: Prevent alert spam with configurable cooldowns
- **Test Framework**: Mock data testing and rule validation

### Frontend (Next.js + TypeScript + Tailwind)
- **Dashboard Interface**: Clean, responsive UI for system management
- **Rules Management**: Full CRUD operations for alert rules
- **Event History**: View and filter recent alert firings
- **Rule Testing**: Interactive testing with mock data and previews
- **Real-time Status**: Live rule status and event monitoring

## 🏗️ Architecture

```
Frontend (Next.js:3000) → Backend (Spring Boot:8080) → MongoDB
                                    ↓
                            Alert Worker (Scheduled)
                                    ↓
                          Multi-Channel Sender
                         (Email, SMS, Extensions)
```

### Core Components

1. **Rule Engine**
   - `Rule.java`: Rule model with conditions, audience, and messaging
   - `ConditionEvaluator.java`: Safe expression evaluation
   - `AudienceResolver.java`: User targeting and filtering
   - `MessageTemplater.java`: Dynamic content generation

2. **Data Sources**
   - `WeatherSourceAdapter.java`: Weather API integration
   - `StatusSourceAdapter.java`: HTTP endpoint monitoring
   - Extensible adapter pattern for additional sources

3. **Alert Worker**
   - `AlertWorker.java`: Scheduled rule evaluation and firing
   - Cooldown management and event tracking
   - Comprehensive test support

4. **REST API**
   - `RuleController.java`: CRUD operations for rules
   - `EventController.java`: Event history and monitoring
   - Cross-origin support for frontend integration

## 🚀 Getting Started

### Prerequisites
- Java 21
- Node.js 18+
- MongoDB (local or remote)
- Maven 3.8+

### Backend Setup

1. **Configure MongoDB** (edit `src/main/resources/application.properties`):
   ```properties
   spring.data.mongodb.uri=mongodb://localhost:27017/alertsystem
   ```

2. **Start Backend**:
   ```bash
   cd Project_API
   mvn clean compile
   mvn exec:java
   ```
   
   Server runs on `http://localhost:8080`

### Frontend Setup

1. **Install Dependencies**:
   ```bash
   cd project_fe
   npm install
   ```

2. **Start Frontend**:
   ```bash
   npm run dev
   ```
   
   Dashboard available at `http://localhost:3000`

## 📋 Usage Guide

### Creating Alert Rules

1. **Navigate to Dashboard**: Open `http://localhost:3000`
2. **Click "New Rule"**: Opens the rule creation form
3. **Configure Rule**:
   - **Name**: Descriptive rule name
   - **Source**: Choose data source (Weather/Status/Custom)
   - **Parameters**: Source-specific configuration
   - **Condition**: JavaScript expression (e.g., `temp_c > 40`)
   - **Message**: Header and content with placeholders `{{variable}}`
   - **Audience**: Tags and/or city targeting
   - **Channels**: Email, SMS, or both
   - **Cooldown**: Minutes between rule firings

### Example Rules

**Extreme Heat Alert**:
```javascript
// Condition: temp_c > 40 && humidity < 20
// Message: "🔥 EXTREME HEAT ALERT: {{temp_c}}°C in {{city}}"
// Audience: Tags ["emergency", "weather"]
```

**Service Downtime Alert**:
```javascript
// Condition: status === "DOWN"
// Message: "🚨 SERVICE DOWN: {{url}} is unavailable"
// Audience: Tags ["ops", "engineering"]
```

### Testing Rules

1. **Click Test Button**: On any rule in the dashboard
2. **Add Mock Data**: JSON data matching your condition variables
3. **Preview**: See rendered message and recipient count
4. **Simulate**: Test audience targeting without sending
5. **Send Test**: Actually fire test alerts to recipients

### Monitoring Events

1. **Events Tab**: View recently fired alerts
2. **Event Details**: Click any event for full information
3. **Channel Results**: See delivery success/failure rates
4. **Source Data**: Inspect the data that triggered the alert

## 🔧 Configuration

### Environment Variables

- `MONGODB_URI`: MongoDB connection string
- `WEATHER_API_KEY`: OpenWeatherMap API key
- `EMAIL_HOST`: SMTP server for email delivery
- `SMS_PROVIDER_*`: SMS provider configuration

### Rule Evaluation Schedule

The `AlertWorker` runs every 5 minutes by default. Modify the `@Scheduled` annotation in `AlertWorker.java` to change frequency:

```java
@Scheduled(fixedRate = 300000) // 5 minutes = 300,000ms
```

### Adding New Data Sources

1. **Create Adapter**: Implement `SourceAdapter` interface
2. **Register Source**: Add to `Source` enum in `Rule.java`
3. **Update Worker**: Add adapter to `AlertWorker` source map

### Extending Channels

1. **Implement Sender**: Create new class implementing channel logic
2. **Update Sender**: Add to `MultiChannelSender.java`
3. **Frontend Support**: Add channel option to form

## 📊 API Endpoints

### Rules
- `GET /api/rules` - List all rules
- `POST /api/rules` - Create new rule
- `PUT /api/rules/{id}` - Update rule
- `DELETE /api/rules/{id}` - Delete rule
- `PATCH /api/rules/{id}/toggle` - Enable/disable rule
- `POST /api/rules/{id}/test` - Test rule with mock data

### Events
- `GET /api/events` - Recent events (limit parameter)
- `GET /api/events/{id}` - Event details
- `GET /api/events/rule/{ruleId}` - Events for specific rule

## 🛡️ Security & Safety

- **Expression Safety**: Sandboxed JavaScript evaluation prevents code injection
- **Input Validation**: All user inputs validated and sanitized
- **Audit Trail**: Complete event logging for compliance
- **Rate Limiting**: Cooldown periods prevent alert spam
- **Test Mode**: Safe testing without affecting production users

## 🔍 Troubleshooting

### Common Issues

1. **Port Conflicts**: Backend (8080) or Frontend (3000) ports in use
   ```bash
   # Find and kill process using port
   netstat -ano | findstr :8080
   taskkill /PID <pid> /F
   ```

2. **MongoDB Connection**: Ensure MongoDB is running and accessible
   - Check connection string in `application.properties`
   - Verify MongoDB service is started

3. **API Proxy Issues**: Frontend can't reach backend
   - Verify backend is running on port 8080
   - Check `next.config.js` proxy configuration

4. **Rule Not Firing**: Check the rule evaluation logs
   - Condition syntax errors
   - No matching audience
   - Cooldown period active

### Logs & Monitoring

- **Backend Logs**: Console output shows rule evaluation and firing
- **Event History**: Dashboard events tab shows firing history
- **Test Results**: Interactive testing provides detailed feedback

## 🎯 Future Enhancements

- **Advanced Scheduling**: Cron-based rule scheduling
- **Rule Dependencies**: Chain rules with conditional dependencies  
- **Escalation Policies**: Multi-tier alert escalation
- **Analytics Dashboard**: Rule performance and recipient engagement
- **Multi-Tenancy**: Organization-based rule isolation
- **Advanced Templating**: Rich HTML email templates
- **Mobile App**: React Native companion app
- **Webhook Integration**: External system notifications

## 📄 License

This project is part of the SJSU CS 272 coursework. All rights reserved.