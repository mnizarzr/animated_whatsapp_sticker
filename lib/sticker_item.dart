import 'package:flutter/material.dart';
import 'package:image/image.dart' as ImageCodec;
import 'package:whatsapp_sticker/sticker.dart';

class StickerPackItem extends StatefulWidget {
  final Sticker sticker;
  final int index;

  StickerPackItem(this.sticker, this.index);

  @override
  _StickerPackItemState createState() => _StickerPackItemState();
}

class _StickerPackItemState extends State<StickerPackItem> {
  String folderName;

  @override
  void initState() {
    List<String> splitted = widget.sticker.directory.path.split("/");
    folderName = splitted[splitted.length - 1];
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      width: MediaQuery.of(context).size.width,
      height: MediaQuery.of(context).size.width / 2,
      decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
          boxShadow: [
            BoxShadow(
                color: Colors.black.withOpacity(0.5),
                spreadRadius: 1,
                blurRadius: 5,
                offset: Offset(0, 2))
          ]),
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.start,
        mainAxisSize: MainAxisSize.max,
        children: <Widget>[
          Text(folderName,
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
          Spacer(),
          SizedBox(
            height: 150,
            child: ListView.builder(
                scrollDirection: Axis.horizontal,
                itemCount: 3,
                itemBuilder: (BuildContext context, int index) {
                  ImageCodec.Image firstFrame = ImageCodec.decodeWebP(
                      widget.sticker.files[index].readAsBytesSync());
                  return Container(
                    margin: const EdgeInsets.only(right: 12.0),
                    child: Center(
                      child: Image.memory(ImageCodec.encodePng(firstFrame),
                          repeat: ImageRepeat.noRepeat,
                          width: 100,
                          height: 100),
                    ),
                  );
                }),
          )
        ],
      ),
    );
  }
}
