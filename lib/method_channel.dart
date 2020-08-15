import 'dart:io';

import 'package:flutter/services.dart';

class PlatformInterface {

  MethodChannel _methodChannel = MethodChannel('whatsapp_sticker');

  bool _isAndroid = Platform.isAndroid;

  static PlatformInterface _instance = PlatformInterface();

  static PlatformInterface get instance => _instance;

  Future<bool> addToWhatsapp(directory) {
    if (_isAndroid)
      return _methodChannel.invokeMethod<bool>(
          "addToWhatsapp", <String, String>{"directory": directory});
    else
      throw UnsupportedError("Functionality only available on Android");
  }
}
