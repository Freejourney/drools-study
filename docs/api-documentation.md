# Drools Study - API Documentation

## Overview

This document provides comprehensive API documentation for the Drools Study application's REST endpoints. The application provides risk control, transaction processing, and analytics capabilities using Drools rules engine.

## Base URL

```
http://localhost:8080/api
```

## Authentication

Currently, the API does not require authentication for development and testing purposes.

## Error Handling

All endpoints return standardized error responses:

```json
{
  "success": false,
  "error": "Error description",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Risk Control API

### Assess Customer Risk

Performs comprehensive risk assessment for a customer.

**Endpoint:** `POST /api/risk/assess/{customerId}`

**Parameters:**
- `customerId` (path): Customer ID to assess

**Request Body:**
```json
{
  "includeTransactionHistory": true,
  "includeCreditHistory": true,
  "daysToAnalyze": 30
}
```

**Response:**
```json
{
  "success": true,
  "customerId": "CUST001",
  "overallRisk": "MEDIUM",
  "riskScore": 65,
  "riskFactors": [
    {
      "factor": "HIGH_VALUE_TRANSACTION",
      "weight": 0.3,
      "description": "Customer has recent high-value transactions"
    }
  ],
  "assessmentDate": "2024-01-15T10:30:00",
  "recommendations": [
    "Monitor customer transactions for unusual patterns"
  ]
}
```

### Generate Risk Report

Generates a detailed risk assessment report.

**Endpoint:** `GET /api/risk/report/{customerId}`

**Parameters:**
- `customerId` (path): Customer ID
- `format` (query, optional): Report format ('JSON', 'PDF'). Default: 'JSON'
- `period` (query, optional): Analysis period in days. Default: 30

**Response:**
```json
{
  "success": true,
  "customerId": "CUST001",
  "reportId": "RPT_2024_001",
  "generatedAt": "2024-01-15T10:30:00",
  "period": 30,
  "summary": {
    "overallRisk": "MEDIUM",
    "totalAlerts": 5,
    "highRiskAlerts": 1,
    "transactionsAnalyzed": 45
  },
  "details": {
    "riskFactors": [...],
    "transactionPatterns": [...],
    "alerts": [...]
  }
}
```

## Transaction API

### Process Transaction

Processes a new transaction with risk validation.

**Endpoint:** `POST /api/transactions/process`

**Request Body:**
```json
{
  "customerId": "CUST001",
  "accountId": "ACC001",
  "amount": 5000.00,
  "transactionType": "WITHDRAWAL",
  "description": "ATM Withdrawal",
  "currency": "USD",
  "merchantName": "Local ATM",
  "merchantCategory": "ATM",
  "location": "New York, NY",
  "channel": "ATM"
}
```

**Response:**
```json
{
  "success": true,
  "transactionId": "TXN_123456",
  "status": "COMPLETED",
  "approved": true,
  "processedAt": "2024-01-15T10:30:00",
  "riskAlerts": [
    {
      "alertId": "ALERT_001",
      "alertType": "HIGH_VALUE_TRANSACTION",
      "riskLevel": "MEDIUM",
      "message": "High value ATM withdrawal detected"
    }
  ]
}
```

### Get Transaction History

Retrieves transaction history for a customer.

**Endpoint:** `GET /api/transactions/history/{customerId}`

**Parameters:**
- `customerId` (path): Customer ID
- `days` (query, optional): Number of days to retrieve. Default: 30

**Response:**
```json
{
  "success": true,
  "customerId": "CUST001",
  "days": 30,
  "transactionCount": 15,
  "transactions": [
    {
      "transactionId": "TXN_123456",
      "amount": 5000.00,
      "transactionType": "WITHDRAWAL",
      "status": "COMPLETED",
      "timestamp": "2024-01-15T10:30:00",
      "location": "New York, NY"
    }
  ]
}
```

### Get Transaction Analytics

Provides analytics for customer transaction patterns.

**Endpoint:** `GET /api/transactions/analytics/{customerId}`

**Parameters:**
- `customerId` (path): Customer ID
- `hours` (query, optional): Analysis period in hours. Default: 24

**Response:**
```json
{
  "success": true,
  "customerId": "CUST001",
  "analysisHours": 24,
  "transactionVelocity": 2.5,
  "transactionVolume": 12500.00,
  "suspiciousPatterns": [
    "RAPID_TRANSACTIONS",
    "ROUND_NUMBER_PATTERN"
  ],
  "analysisTime": "2024-01-15T10:30:00"
}
```

### Create Sample Transaction

Creates a sample transaction for testing purposes.

**Endpoint:** `POST /api/transactions/sample`

**Parameters:**
- `customerId` (query): Customer ID for the sample transaction

**Response:**
```json
{
  "success": true,
  "message": "Sample transaction created and processed",
  "transaction": {
    "transactionId": "SAMPLE_001",
    "amount": 100.00,
    "transactionType": "DEBIT",
    "status": "COMPLETED"
  },
  "result": {
    "approved": true,
    "status": "COMPLETED",
    "riskAlerts": []
  }
}
```

## Health Check

### Service Health

Checks the health status of the transaction service.

**Endpoint:** `GET /api/transactions/health`

**Response:**
```json
{
  "status": "UP",
  "service": "TransactionController",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Error Codes

| Code | Description |
|------|-------------|
| 400  | Bad Request - Invalid input parameters |
| 404  | Not Found - Resource not found |
| 500  | Internal Server Error - Server processing error |

## Rate Limiting

- **Default Rate Limit:** 1000 requests per hour per IP
- **Burst Limit:** 50 requests per minute per IP

Rate limit headers are included in responses:
- `X-RateLimit-Limit`: Request limit per hour
- `X-RateLimit-Remaining`: Remaining requests in current window
- `X-RateLimit-Reset`: Time when the rate limit resets

## Examples

### Risk Assessment Example

```bash
curl -X POST http://localhost:8080/api/risk/assess/CUST001 \
  -H "Content-Type: application/json" \
  -d '{
    "includeTransactionHistory": true,
    "includeCreditHistory": true,
    "daysToAnalyze": 30
  }'
```

### Transaction Processing Example

```bash
curl -X POST http://localhost:8080/api/transactions/process \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "accountId": "ACC001",
    "amount": 1500.00,
    "transactionType": "WITHDRAWAL",
    "description": "ATM Withdrawal",
    "currency": "USD",
    "location": "New York, NY",
    "channel": "ATM"
  }'
```

### Transaction History Example

```bash
curl -X GET "http://localhost:8080/api/transactions/history/CUST001?days=7"
```

## SDKs and Libraries

Currently, we provide REST API access. SDKs for popular programming languages are planned for future releases:

- Java SDK (Planned)
- Python SDK (Planned)
- JavaScript/Node.js SDK (Planned)

## Support

For API support and questions:
- Email: api-support@drools-study.com
- Documentation: [GitHub Repository](https://github.com/drools-study)
- Issues: [GitHub Issues](https://github.com/drools-study/issues)

## Changelog

### Version 1.0.0 (Current)
- Initial API release
- Risk assessment endpoints
- Transaction processing endpoints
- Basic analytics endpoints

### Planned Features
- Batch processing endpoints
- Advanced analytics
- Machine learning integration
- Real-time webhooks 