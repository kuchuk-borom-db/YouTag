class ExceptionResponse {
  final int status;
  final String? msg;

  ExceptionResponse(this.msg, this.status);

  String getMsg() {
    String output = "Status : $status";
    if (msg != null) {
      output = "$output, $msg";
    }
    return output;
  }
}
