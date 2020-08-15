import 'dart:collection';

/// Using mime types instead of only using extensions
/// for further development
class MimeTypeResolver {

  static String lookupMimeType(String path) {
    String result;
    var ext = _ext(path);
    result = _mimeHashMaps[ext];
    if(result != null) return result;
    return null;
  }

  static String _ext(String path) {
    var lastDot = path.lastIndexOf(".");
    if (lastDot != -1) {
      return path.substring(lastDot + 1).toLowerCase();
    } else
      return null;
  }

  /// Allowed mime type
  static Map<String, String> _mimeMaps = {
    'gif': 'image/gif',
    'jpeg': 'image/jpeg',
    'jpg': 'image/jpeg',
    'png': 'image/png',
    'webp': 'image/webp'
  };

  static HashMap<String, String> _mimeHashMaps =
      HashMap<String, String>.from(_mimeMaps);

  static UnmodifiableMapView mapView = UnmodifiableMapView(_mimeMaps);

}
