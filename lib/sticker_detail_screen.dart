import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:image/image.dart' as ImageCodec;
import 'package:whatsapp_sticker/method_channel.dart';
import 'package:whatsapp_sticker/sticker.dart';

class StickerDetailScreen extends StatefulWidget {
  final Sticker sticker;

  StickerDetailScreen(this.sticker);

  @override
  _StickerDetailScreenState createState() => _StickerDetailScreenState();
}

class _StickerDetailScreenState extends State<StickerDetailScreen> {
  Uint8List trayImage;
  String title;

  PlatformInterface _platform = PlatformInterface.instance;

  Future<bool> _addToWhatsApp() async {
    var keceluk = await _platform.addToWhatsapp(widget.sticker.directory.path);
    return keceluk;
  }

  @override
  void initState() {
    ImageCodec.Image image =
        ImageCodec.decodeWebP(widget.sticker.files[0].readAsBytesSync());
    trayImage = ImageCodec.encodePng(image);
    List<String> splitted = widget.sticker.directory.path.split("/");
    title = splitted[splitted.length - 1];
    super.initState();
  }

  @override
  Widget build(context) {
    return Scaffold(
        appBar: AppBar(title: Text("WhatsApp Sticker")),
        body: Column(
          children: [
            _buildHeader(context),
            _buildStickers(context),
            _buildFooter(context)
          ],
        ));
  }

  Widget _buildHeader(BuildContext context) {
    return Container(
      decoration: BoxDecoration(color: Theme.of(context).canvasColor),
      height: 150,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Image.memory(
            trayImage,
            repeat: ImageRepeat.noRepeat,
            width: 80,
            height: 80,
          ),
          Text(
            title,
            style: TextStyle(fontSize: 24),
          )
        ],
      ),
    );
  }

  Widget _buildStickers(BuildContext context) {
    return Expanded(
      child: CustomScrollView(
        primary: false,
        shrinkWrap: true,
        slivers: [
          SliverPadding(
            padding: const EdgeInsets.all(12),
            sliver: SliverGrid.count(
              crossAxisSpacing: 6,
              mainAxisSpacing: 6,
              crossAxisCount: 5,
              children: widget.sticker.files
                  .map((File file) => GestureDetector(
                        onTap: () {
                          showDialog(
                              context: context,
                              builder: (context) {
                                return AlertDialog(
                                    contentPadding: const EdgeInsets.all(6.0),
                                    content: Container(
                                      height: 200,
                                      child: Image.file(
                                        file,
                                        width: 80,
                                        height: 80,
                                      ),
                                    ),
                                    actions: [
                                      FlatButton(
                                          onPressed: Navigator.of(context).pop,
                                          child: Text("Close"))
                                    ]);
                              });
                        },
                        child: Image.file(
                          file,
                          frameBuilder:
                              (context, child, frame, wasSynchronouslyLoaded) {
                            return AnimatedOpacity(
                              child: child,
                              opacity: frame == null ? 0 : 1,
                              duration: const Duration(seconds: 1),
                              curve: Curves.easeOut,
                            );
                          },
                          width: 30,
                          height: 30,
                        ),
                      ))
                  .toList(),
            ),
          )
        ],
      ),
    );
  }

  Widget _buildFooter(BuildContext context) {
    return Center(
        child: Padding(
      padding: const EdgeInsets.all(12.0),
      child: RaisedButton(
        splashColor: Color(0xFF3EBE4F),
        onPressed: () {
          _addToWhatsApp();
        },
        padding: const EdgeInsets.all(12.0),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8.0)),
        child: const Text("Add to WhatsApp",
            style: const TextStyle(color: Colors.white, fontSize: 18)),
      ),
    ));
  }
}
