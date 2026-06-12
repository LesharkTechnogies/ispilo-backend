# Ispilo Authentication & Verification Flow

## Important: Phone Number Normalization
The TalkSasa SMS gateway strictly requires phone numbers to be in the `2547XXXXXXXX` format (without the `+` sign or leading `0`).
- **Frontend:** You can safely allow users to input `07XXXXXXXX` or `01XXXXXXXX` in the UI.
- **Backend (Implemented):** The backend's `SmsService` intercepts the request and automatically strips spaces, dashes, parentheses, and the `+` sign. It then converts local Kenyan formats (`07...` or `01...`) to `254...` right before sending the SMS.

---

## 1. Registration Flow
**Endpoint:** `POST /api/v1/auth/register`  
**Headers:** `X-Device-ID: <device-id>`

**Request Payload:**
```json
{
  "email": "user@example.com",
  "password": "secret123",
  "firstName": "Jane",
  "lastName": "Doe",
  "phone": "+2547XXXXXXXX",
  "countryCode": "KE",
  "county": "Nairobi",
  "town": "Westlands"
}
```

**Response Payload (201 Created):**
```json
{
  "success": true,
  "message": "Verification code sent successfully",
  "data": {
    "phone": "+2547XXXXXXXX",
    "requiresVerification": true
  }
}
```

- **Frontend Action:** User fills out the registration form (Email, Password, Name, Phone).
- **Token Handling:** 🛑 **NO AUTH TOKEN IS RETURNED HERE.** Do not attempt to log the user in or save tokens.
- **Navigation:** Redirect the user to the `VerifyPhoneScreen`, passing the `phone` and `isPasswordReset: false`.

---

## 2. Phone Verification Flow (Post-Registration)

### Step 2A: Verify Code
**Endpoint:** `POST /api/v1/auth/verify-phone`

**Request Payload:**
```json
{
  "phone": "+2547XXXXXXXX",
  "code": "123456"
}
```

**Response Payload (200 OK):**
```json
{
  "success": true,
  "message": "Phone verified and registration completed successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI...",
    "refreshToken": "eyJhbGciOiJIUzI...",
    "user": {
      "id": "uuid-v4",
      "email": "user@example.com",
      "name": "Jane Doe",
      "phone": "+2547XXXXXXXX",
      "isVerified": true
    }
  }
}
```
- **Frontend Action:** The user inputs the 6-digit code into your 6-Box PIN UI.
- **Token Handling:** ✅ **SAVE THE TOKENS HERE.** Securely store the `token` and `refreshToken` in device storage.
- **Navigation:** Navigate the user into the main app (Home Screen) as a fully logged-in and verified user.

### Step 2B: Resend Verification Code
**Endpoint:** `POST /api/v1/auth/resend-phone-code`

**Request Payload:**
```json
{
  "phone": "+2547XXXXXXXX"
}
```

**Response Payload (200 OK):**
```json
{
  "success": true,
  "message": "Verification code resent successfully"
}
```

---

## 3. Login Flow
**Endpoint:** `POST /api/v1/auth/login`  
**Headers:** `X-Device-ID: <device-id>` *(Required: A unique device identifier to prevent banned devices from logging in)*

**Request Payload:**
```json
{
  "phone": "+2547XXXXXXXX",
  "password": "secret123"
}
```

**Response Payload (200 OK):**
*(Note: The login endpoint returns the auth object directly, not wrapped in success/message fields)*
```json
{
  "token": "eyJhbGciOiJIUzI...",
  "refreshToken": "eyJhbGciOiJIUzI...",
  "user": {
    "id": "uuid-v4",
    "email": "user@example.com",
    "name": "Jane Doe",
    "phone": "+2547XXXXXXXX",
    "isVerified": true
  }
}
```

**Error Handling (Frontend Logic):**
- **401 Unauthorized (`Phone number not verified.`)**: If a user tries to log in but their `isPhoneVerified` status is false in the backend, the login will fail. 
  - *Action:* The frontend should catch this specific error and navigate the user to the `VerifyPhoneScreen`. Automatically trigger the `resend-phone-code` endpoint behind the scenes to send them a fresh OTP, and let them verify their account.
- **401 Unauthorized / 403 Forbidden (`Device is banned` or `Account is flagged`)**: Show a persistent error message asking the user to contact support.
- **401 Bad Credentials (`Invalid phone number or password`)**: Show standard "Invalid credentials" error.

**Success Flow (Frontend Logic):**
- **Token Handling:** ✅ **SAVE THE TOKENS HERE.** Securely store the `token` (JWT) and `refreshToken` in device storage (e.g., `SecureStore` in React Native, or HttpOnly cookies / LocalStorage for Web). Overwrite any existing tokens.
- **State Management:** Update your global state manager (Redux, Context, Zustand) to set `isAuthenticated: true` and store the `user` profile data.
- **Navigation:** Navigate to the Main App (Home Screen).

---

## 3.5 Making Authenticated Requests (e.g., Creating a Post)

Once the user is logged in (via Login or Registration Verification), you possess the `token` (JWT). This token is required to make requests to protected endpoints, such as creating a post, commenting, or viewing a user profile.

**Header Requirement:**
You must attach the token to the `Authorization` header as a Bearer token in every protected HTTP request.

**Example: Creating a Post**
**Endpoint:** `POST /api/v1/posts`
**Headers:**
```http
Authorization: Bearer eyJhbGciOiJIUzI...
Content-Type: application/json
```

**Frontend Axios Interceptor Example:**
```javascript
import axios from 'axios';

const api = axios.create({ baseURL: 'https://api.yourdomain.com' });

api.interceptors.request.use(async (config) => {
  const token = await getStoredToken(); // Retrieve from Secure Storage
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

---

## 4. Forgot Password Flow

### Step 4A: Request Reset Code
**Endpoint:** `POST /api/v1/auth/forgot-password/request-code`

**Request Payload:**
```json
{
  "phone": "+2547XXXXXXXX"
}
```

**Response Payload (200 OK):**
```json
{
  "success": true,
  "message": "Verification code sent"
}
```
- **Navigation:** Redirect to `VerifyPhoneScreen`, passing `phone` and `isPasswordReset: true`.

### Step 4B: Resend Reset Code
**Endpoint:** `POST /api/v1/auth/forgot-password/resend-code`

**Request Payload:**
```json
{
  "phone": "+2547XXXXXXXX"
}
```

**Response Payload (200 OK):**
```json
{
  "success": true,
  "message": "Verification code sent"
}
```

### Step 4C: Verifying the Code & Resetting Password
**Endpoint:** `POST /api/v1/auth/forgot-password/reset`

**Optimal Interaction Flow:**
1. User enters the 6-digit code on the `VerifyPhoneScreen`.
2. Because `isPasswordReset` is true, **do not call an API yet**. Instead, pass the `phone` and `code` variables forward to a new screen: `CreateNewPasswordScreen`.
3. On `CreateNewPasswordScreen`, the user enters their "New Password" and "Confirm Password".
4. Submit the complete payload to the backend.

**Request Payload:**
```json
{
  "phone": "+2547XXXXXXXX",
  "code": "123456",
  "newPassword": "newSecret123",
  "confirmPassword": "newSecret123"
}
```

**Response Payload (200 OK):**
```json
{
  "success": true,
  "message": "Password updated successfully"
}
```
- **Token Handling:** 🛑 **NO AUTH TOKEN IS RETURNED.** 
- **Navigation:** Redirect the user back to the `LoginScreen`. They must manually log in with their newly created password to receive their auth tokens.