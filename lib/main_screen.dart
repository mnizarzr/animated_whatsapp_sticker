import 'dart:io';

import 'package:flutter/cupertino.dart';
import "package:flutter/material.dart";
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:whatsapp_sticker/mime_type_resolver.dart';
import 'package:whatsapp_sticker/sticker.dart';
import 'package:whatsapp_sticker/sticker_detail_screen.dart';
import 'package:whatsapp_sticker/sticker_item.dart';

class MainScreen extends StatefulWidget {
  @override
  _MainScreenState createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  static const int MAX_SIZE = 30;

  final Permission _storagePermission = Permission.storage;
  PermissionStatus _storagePermissionStatus = PermissionStatus.undetermined;

  bool loading;

  List<FileSystemEntity> subDirs;
  Directory stickerDir;

  final List<Sticker> stickers = List<Sticker>();

  @override
  void initState() {
    super.initState();

    _listenForPermissionStatus();
    _askStoragePermission();
    if (_storagePermissionStatus.isGranted) {
      stickerDir = Directory("/sdcard/Stickers/");
      _getSubDirs();
    }
  }

  void _listenForPermissionStatus() async {
    final status = await _storagePermission.status;
    setState(() => _storagePermissionStatus = status);
  }

  Future<void> _askStoragePermission() async {
    final status = await _storagePermission.request();

    setState(() {
      _storagePermissionStatus = status;
    });

    if (_storagePermissionStatus.isGranted) {
      stickerDir = Directory("/sdcard/Stickers/"); // may error, looking for another alternatives
      _getSubDirs();
    }
  }

  void _getSubDirs() async {
    setState(() {
      loading = true;
    });

    try {
      var directoryExist = await stickerDir.exists();
      if (directoryExist) {
        subDirs = stickerDir.listSync();
      } else {
        stickerDir.create();
      }
      _getStickers();
    } catch (err) {
      print(err);
    }
  }

  _getStickers() {
    List<Sticker> foldersContainStickers = [];

    subDirs.forEach((dir) {
      if (dir is Directory) {
        List<File> imageFiles = List<File>();
        dir.listSync().forEach((e) {
          if (e is File) {
            String mimeType = MimeTypeResolver.lookupMimeType(e.path);

            /// if only allowed extension / mime type;
            if (mimeType != null && imageFiles.length < MAX_SIZE)
              imageFiles.add(e);
          }
        });
        foldersContainStickers.add(Sticker(dir, imageFiles));
      }
    });

    setState(() {
      stickers.addAll([...foldersContainStickers]);
      loading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(title: Text("WhatsApp Sticker")),
        body: _storagePermissionStatus.isGranted
            ? _buildList(context)
            : _buildAskPermission(context));
  }

  Widget _buildList(BuildContext context) {
    return loading
        ? _buildLoading(context)
        : ListView.builder(
            padding: const EdgeInsets.all(12),
            itemCount: stickers.length,
            itemBuilder: (BuildContext context, int index) {
              return FlatButton(
                  onPressed: () {
                    Navigator.push(
                        context,
                        MaterialPageRoute(
                            builder: (context) =>
                                StickerDetailScreen(stickers[index])));
                  },
                  child: StickerPackItem(stickers[index], index));
            });
  }

  Widget _buildLoading(BuildContext context) {
    return Center(child: CircularProgressIndicator());
  }

  Widget _buildAskPermission(BuildContext context) {
    return Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Text(
              "Aplikasi ini membutuhkan izin untuk mengakses penyimpanan Anda",
              style: TextStyle(fontSize: 18),
              textAlign: TextAlign.center,
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Text(
              "Tambahkan sticker di folder \"Stickers\"",
              style: TextStyle(fontSize: 18),
              textAlign: TextAlign.center,
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: RaisedButton(
                textColor: Colors.white,
                padding: const EdgeInsets.all(0.0),
                onPressed: () {
                  _askStoragePermission();
                },
                child: Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: Theme.of(context).primaryColor,
                    ),
                    child: Text("Minta izin akses penyimpanan"))),
          )
        ]);
  }
}
