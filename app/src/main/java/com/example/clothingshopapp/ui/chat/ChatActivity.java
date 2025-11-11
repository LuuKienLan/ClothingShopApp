package com.example.clothingshopapp.ui.chat; // (Package của "bố")

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull; // ⭐ THÊM IMPORT NÀY
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.ui.adapter.ChatAdapter;
import com.example.clothingshopapp.model.Message; // <-- Dùng model.Message (zin)
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener; // ⭐ THÊM IMPORT NÀY
import com.google.android.gms.tasks.Task; // ⭐ THÊM IMPORT NÀY
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration; // ⭐ THÊM IMPORT NÀY
import com.google.firebase.firestore.Query;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private static final String SHOP_UID = "leTUI0RSJGaAZDLe0qZhC5LlP1p2";

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageView sendButton;
    private ImageView backArrow;
    private TextView titleText;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ChatAdapter adapter;

    private String currentUserId;
    private String chatId;
    private ListenerRegistration unreadListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        String chatTitle = getIntent().getStringExtra("CHAT_TITLE");
        String intentChatId = getIntent().getStringExtra("CHAT_ID");

        // Logic Toolbar (Sửa Tiếng Việt)
        backArrow = findViewById(R.id.back_arrow);
        titleText = findViewById(R.id.title_text);

        // ⭐⭐⭐ BẮT ĐẦU SỬA (DỊCH SANG TIẾNG ANH) ⭐⭐⭐
        if (chatTitle != null && !chatTitle.isEmpty()) {
            titleText.setText(chatTitle); // Tên "Khách hàng" (đã là Tiếng Anh)
        } else {
            titleText.setText("Customer Support"); // Sửa từ "Hỗ trợ khách hàng"
        }
        // ⭐⭐⭐ KẾT THÚC SỬA ⭐⭐⭐

        backArrow.setOnClickListener(v -> {
            finish();
        });

        // Ánh xạ View (Giữ nguyên)
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Logic "Chia đường" (Giữ nguyên)
        if (intentChatId != null) {
            Log.d(TAG, "Admin đang vào. Tải phòng chat ID: " + intentChatId);
            this.chatId = intentChatId;
            onChatRoomReady();
        } else {
            Log.d(TAG, "User đang vào. Tự động tìm/tạo phòng...");
            findOrCreateChatRoom();
        }
    }

    private void findOrCreateChatRoom() {
        // ... (GIỮ NGUYÊN)
        db.collection("Chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String foundChatId = null;
                        for (DocumentSnapshot document : task.getResult()) {
                            List<String> participants = (List<String>) document.get("participants");
                            if (participants != null && participants.contains(SHOP_UID)) {
                                foundChatId = document.getId();
                                break;
                            }
                        }
                        if (foundChatId != null) {
                            Log.d(TAG, "Đã tìm thấy phòng chat, ID: " + foundChatId);
                            this.chatId = foundChatId;
                            onChatRoomReady();
                        } else {
                            Log.d(TAG, "Không tìm thấy phòng chat, đang tạo phòng mới...");
                            createChatRoom();
                        }
                    } else {
                        Log.e(TAG, "Lỗi khi tìm phòng chat: ", task.getException());
                        Toast.makeText(this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void createChatRoom() {
        // ... (GIỮ NGUYÊN - Đã sửa lỗi "Khách hàng")
        db.collection("Users").document(currentUserId).get()
                .addOnCompleteListener(userTask -> {

                    if (!userTask.isSuccessful() || userTask.getResult() == null) {
                        Log.e(TAG, "Lỗi khi lấy hồ sơ user lúc tạo phòng chat", userTask.getException());
                        Toast.makeText(this, "Lỗi: Không thể lấy hồ sơ user", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String userFullName = "Khách hàng"; // Tên mặc định nếu có lỗi
                    if (userTask.getResult().exists()) {
                        String nameFromDb = userTask.getResult().getString("fullName");
                        if (nameFromDb != null && !nameFromDb.isEmpty()) {
                            userFullName = nameFromDb;
                        }
                    }

                    CollectionReference chatsRef = db.collection("Chats");
                    Map<String, Object> newChat = new HashMap<>();
                    newChat.put("participants", Arrays.asList(currentUserId, SHOP_UID));
                    newChat.put("lastMessage", "...");
                    newChat.put("lastTimestamp", FieldValue.serverTimestamp());
                    newChat.put("userUnreadCount", 0);
                    newChat.put("shopUnreadCount", 0);
                    newChat.put("userFullName", userFullName);
                    newChat.put("userAvatarUrl", "");

                    chatsRef.add(newChat)
                            .addOnSuccessListener(documentReference -> {
                                this.chatId = documentReference.getId();
                                onChatRoomReady();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Lỗi khi tạo phòng chat: ", e);
                                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }


    // (Hàm onChatRoomReady giữ nguyên)
    private void onChatRoomReady() {
        setupSendButton();
        setupChatRecyclerView();
        startUnreadResetListener(); // (Code fix "nháy" lần trước)
    }

    // (Hàm startUnreadResetListener giữ nguyên)
    private void startUnreadResetListener() {
        if (chatId == null || mAuth.getCurrentUser() == null) return;
        String fieldToReset;
        if (currentUserId.equals(SHOP_UID)) {
            fieldToReset = "shopUnreadCount";
        } else {
            fieldToReset = "userUnreadCount";
        }
        Log.d(TAG, "Bắt đầu 'nghe' để reset '" + fieldToReset + "' về 0.");
        if (unreadListener != null) {
            unreadListener.remove();
        }
        unreadListener = db.collection("Chats").document(chatId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Lỗi khi 'nghe' unread count", error);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        Long count = snapshot.getLong(fieldToReset);
                        if (count != null && count > 0) {
                            Log.d(TAG, "Phát hiện '" + fieldToReset + "' = " + count + ". Lập tức reset về 0.");
                            snapshot.getReference().update(fieldToReset, 0);
                        }
                    }
                });
    }

    // (Hàm setupSendButton và sendMessage giữ nguyên - đã có "chống spam")
    private void setupSendButton() {
        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                sendButton.setEnabled(false);
                sendMessage(messageText);
            }
        });
    }

    private void sendMessage(String messageText) {
        Message message = new Message(
                messageText,
                currentUserId,
                Timestamp.now()
        );
        db.collection("Chats").document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Gửi tin nhắn vào subcollection thành công");
                    messageInput.setText("");
                    sendButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Lỗi khi gửi tin nhắn vào subcollection", e);
                    sendButton.setEnabled(true);
                });
        DocumentReference chatDocRef = db.collection("Chats").document(chatId);
        String unreadFieldToIncrement;
        if (currentUserId.equals(SHOP_UID)) {
            unreadFieldToIncrement = "userUnreadCount";
        } else {
            unreadFieldToIncrement = "shopUnreadCount";
        }
        chatDocRef.update(
                "lastMessage", messageText,
                "lastTimestamp", FieldValue.serverTimestamp(),
                unreadFieldToIncrement, FieldValue.increment(1)
        ).addOnFailureListener(e -> Log.w(TAG, "Lỗi khi cập nhật lastMessage", e));
    }

    // (Hàm setupChatRecyclerView giữ nguyên - đã fix "trống trơn")
    private void setupChatRecyclerView() {
        Query query = db.collection("Chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .build();
        adapter = new ChatAdapter(options);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                chatRecyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        });

        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // (Trống là đúng rồi, vì 'startListening' ở setupChatRecyclerView)
    }

    // (Hàm onPause và onStop giữ nguyên - đã fix "nháy" số)
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Màn hình Chat bị 'pause'.");

        if (chatId == null) return;

        if (currentUserId.equals(SHOP_UID)) {
            Log.d(TAG, "Shop 'pause' ('onPause'), dọn dẹp 'shopUnreadCount' về 0.");
            db.collection("Chats").document(chatId)
                    .update("shopUnreadCount", 0)
                    .addOnFailureListener(e -> Log.w(TAG, "Lỗi khi reset shopUnreadCount lúc onPause"));
        } else {
            Log.d(TAG, "User 'pause' ('onPause'), dọn dẹp 'userUnreadCount' về 0.");
            db.collection("Chats").document(chatId)
                    .update("userUnreadCount", 0)
                    .addOnFailureListener(e -> Log.w(TAG, "Lỗi khi reset userUnreadCount lúc onPause"));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Màn hình Chat bị 'stop'.");

        if (adapter != null) {
            adapter.stopListening();
        }

        if (unreadListener != null) {
            Log.d(TAG, "onStop: Bịt 'tai nghe' reset unread count.");
            unreadListener.remove();
            unreadListener = null;
        }
    }
}