package com.github.spook.masks.enums;

public enum ManagerKillResult {
  SUCCESS("Successfully killed MaskManager"),
  FAILED("Failed to kill MaskManager");

  private final String message;

  ManagerKillResult(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
