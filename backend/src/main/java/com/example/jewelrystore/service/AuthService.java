package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;

public interface AuthService {
  LoginResponse register(RegisterRequest request);

  LoginResponse login(LoginRequest request);

  LoginResponse staffLogin(StaffLoginRequest request);

  UserResponse me();

  void logout();

  void changePassword(PasswordRequest request);

  void forgotPassword(ForgotPasswordRequest request);

  void resetPassword(ResetPasswordRequest request);
}
