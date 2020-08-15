import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:io';
import 'main_screen.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    SystemChrome.setPreferredOrientations(
        [DeviceOrientation.portraitUp, DeviceOrientation.portraitDown]);

    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'WhatsApp Sticker by Nizar',
      theme: ThemeData(
          primarySwatch: Colors.blue
      ),
      home: MainScreen(),
    );
  }
}

class Coba extends StatelessWidget {
  @override
  Widget build(context) {
    return Scaffold(
      appBar: AppBar(title: Text("Nyoba")),
      body: Center(
        child: Image.file(
          File("/sdcard/Stickers/punymove/punymove_agad2xkaaribghy.webp"),
          scale: 1,
        ),
      ),
    );
  }
}
