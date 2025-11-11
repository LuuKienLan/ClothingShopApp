const functions = require("firebase-functions");
const crypto = require("crypto");
const cors = require("cors")({ origin: true });
const querystring = require("qs");

// Lấy thông tin cấu hình bảo mật bạn đã set
const vnp_TmnCode = functions.config().vnpay.tmncode;
const vnp_HashSecret = functions.config().vnpay.secretkey;
const vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
// URL trả về *SAU KHI* thanh toán. Đây phải là Deep Link của app bạn
const vnp_ReturnUrl = "myapp://payment-result"; // Quan trọng: Phải khớp với AndroidManifest

/**
 * Hàm này được gọi từ app Android
 * data: { amount: 100000, orderId: "UNIQUE_ORDER_ID123" }
 */
exports.createVnpayPaymentUrl = functions.https.onCall(async (data, context) => {
  // Đảm bảo user đã đăng nhập
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "Function này cần được xác thực."
    );
  }

  process.env.TZ = "Asia/Ho_Chi_Minh"; // Set múi giờ Việt Nam
  const date = new Date();
  const createDate = formatDate(date, "yyyyMMddHHmmss");
  
  const ipAddr = context.rawRequest.ip || "127.0.0.1";
  const amount = data.amount;
  const orderId = data.orderId; // Mã đơn hàng duy nhất từ app

  let vnp_Params = {};
  vnp_Params["vnp_Version"] = "2.1.0";
  vnp_Params["vnp_Command"] = "pay";
  vnp_Params["vnp_TmnCode"] = vnp_TmnCode;
  vnp_Params["vnp_Locale"] = "vn";
  vnp_Params["vnp_CurrCode"] = "VND";
  vnp_Params["vnp_TxnRef"] = orderId;
  vnp_Params["vnp_OrderInfo"] = "Thanh toan don hang " + orderId;
  vnp_Params["vnp_OrderType"] = "other";
  vnp_Params["vnp_Amount"] = amount * 100; // VNPAY yêu cầu nhân 100
  vnp_Params["vnp_ReturnUrl"] = vnp_ReturnUrl;
  vnp_Params["vnp_IpAddr"] = ipAddr;
  vnp_Params["vnp_CreateDate"] = createDate;

  vnp_Params = sortObject(vnp_Params);

  const signData = querystring.stringify(vnp_Params, { encode: false });
  const hmac = crypto.createHmac("sha512", vnp_HashSecret);
  const signed = hmac.update(Buffer.from(signData, "utf-8")).digest("hex");
  vnp_Params["vnp_SecureHash"] = signed;

  const paymentUrl = vnp_Url + "?" + querystring.stringify(vnp_Params, { encode: true });

  // Trả URL về cho app
  return { paymentUrl: paymentUrl };
});

// --- Các hàm hỗ trợ ---
function sortObject(obj) {
  let sorted = {};
  let str = [];
  let key;
  for (key in obj) {
    if (obj.hasOwnProperty(key)) {
      str.push(encodeURIComponent(key));
    }
  }
  str.sort();
  for (key = 0; key < str.length; key++) {
    sorted[str[key]] = encodeURIComponent(obj[str[key]]).replace(/%20/g, "+");
  }
  return sorted;
}

function formatDate(date, format) {
  const map = {
    MM: ("0" + (date.getMonth() + 1)).slice(-2),
    dd: ("0" + date.getDate()).slice(-2),
    yyyy: date.getFullYear(),
    HH: ("0" + date.getHours()).slice(-2),
    mm: ("0" + date.getMinutes()).slice(-2),
    ss: ("0" + date.getSeconds()).slice(-2),
  };
  return format.replace(/MM|dd|yyyy|HH|mm|ss/g, (matched) => map[matched]);
}