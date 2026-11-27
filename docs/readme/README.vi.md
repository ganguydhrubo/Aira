<!-- translated-from: README.md @ 96cbdcf672d1 -->
<div align="center">

<img src="../media/logo.png" alt="Aira" width="88" />

# Aira

**Trợ lý AI trên điện thoại của bạn: cứ nói điều bạn cần, nó sẽ thao tác các ứng dụng và web cho đến khi xong việc.**

Nó làm việc trên một màn hình nền ẩn, nên bạn vẫn dùng điện thoại như bình thường.

<br/>

<a href="https://github.com/ganguydhrubo/Aira/releases/latest"><img src="https://img.shields.io/badge/Download%20APK-Android%2011%2B-2ea043?style=for-the-badge&logo=android&logoColor=white" alt="Tải APK" /></a> <a href="https://github.com/ganguydhrubo/Aira"><img src="https://img.shields.io/github/stars/ganguydhrubo/Aira?style=for-the-badge&logo=github&label=Star&color=FFD700&logoColor=white" alt="Cho repo này một sao" /></a>

<br/>

<a href="https://github.com/ganguydhrubo/Aira/releases"><img src="https://img.shields.io/github/downloads/ganguydhrubo/Aira/total?label=downloads&color=2ea043" alt="lượt tải" /></a> <a href="https://github.com/ganguydhrubo/Aira/releases/latest"><img src="https://img.shields.io/github/v/release/ganguydhrubo/Aira?include_prereleases&label=version" alt="phiên bản" /></a> <a href="../../LICENSE"><img src="https://img.shields.io/badge/license-Apache--2.0-lightgrey" alt="giấy phép" /></a>

<br/>

[English](../../README.md) · [简体中文](README.zh-CN.md) · [繁體中文](README.zh-Hant.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [Español](README.es.md) · [Türkçe](README.tr.md) · Tiếng Việt

<sub>Bản tiếng Việt này được dịch từ README.md tiếng Anh. Nếu thấy chỗ nào nghe chưa tự nhiên hoặc chưa khớp với bản gốc, rất mong bạn góp ý sửa qua một PR.</sub>

<br/>

</div>

## Vì sao chọn Aira

| | |
|---|---|
| 📱 **Không chiếm màn hình của bạn** | Mở ứng dụng, chạm, gõ và cuộn trên một màn hình nền riêng trong khi bạn dùng điện thoại như bình thường; chạm vào khung xem trực tiếp nhỏ bất cứ lúc nào bạn muốn theo dõi |
| 🧩 **Hoạt động với mọi ứng dụng** | Nhìn vào màn hình và tìm nút bấm như một con người: không cần API của ứng dụng, không cần tích hợp riêng cho từng ứng dụng |
| 🌐 **Gọi nó từ bất kỳ trang web nào** | Trong trình duyệt tích hợp, hãy nói điều bạn muốn ngay trên trang bạn đang đọc và nó sẽ làm việc ngay tại đó; chạm vào trang là nó dừng lại |
| 🧠 **Càng dùng càng giỏi** | Ghi nhớ các sở thích bạn cho nó biết, và biến những việc đã hoàn thành thành kỹ năng để lần sau nhanh và ổn định hơn |
| 🔒 **Dữ liệu của bạn ở lại trên điện thoại** | Không có máy chủ Aira nào cả; dùng khóa API mô hình của riêng bạn, hoặc đăng nhập bằng tài khoản ChatGPT, xAI hoặc OpenRouter |
| 🆓 **Mã nguồn mở và miễn phí** | Apache-2.0, toàn bộ mã đều công khai |

## Bắt đầu trong ba bước

1. **Cài đặt**: tải APK từ [Releases](https://github.com/ganguydhrubo/Aira/releases/latest).
2. **Kết nối mô hình**: trong Settings › Model (Cài đặt › Mô hình), chọn một nhà cung cấp và nhập khóa API, hoặc chỉ cần đăng nhập bằng tài khoản ([mô hình và đăng nhập](../models.en.md)).
3. **Để điều khiển ứng dụng trên điện thoại**: làm theo trình hướng dẫn "Set up phone control" (Thiết lập điều khiển điện thoại) trong Cài đặt và ghép nối gỡ lỗi không dây trên điện thoại một lần ([hướng dẫn thiết lập](../setup-guide.en.md)). Bỏ qua bước này nếu bạn chỉ muốn trò chuyện, web và tệp.

Sau đó thử nói: "Đặt báo thức lúc 9:40 và 10:00 sáng mai" hoặc "Kiểm tra thời tiết lúc 8 giờ mỗi sáng và chỉ báo cho tôi nếu trời sẽ mưa".

> 💡 Có máy tính bên cạnh? Gửi câu này cho AI agent trên máy tính của bạn và nó sẽ cài đặt mọi thứ giúp bạn:<br/>
> `Làm theo https://github.com/ganguydhrubo/Aira/blob/main/docs/install-with-agent.en.md để cài Aira lên điện thoại Android đang kết nối qua USB.`

## Aira làm được gì

| | |
|---|---|
| **Điều khiển ứng dụng** | Đọc danh sách dài và lịch sử trò chuyện, chạm và gõ, chia sẻ văn bản vào các ứng dụng; phần đăng nhập, mã xác minh và thanh toán nó để lại cho bạn |
| **Duyệt web** | Một trình duyệt tích hợp đẳng cấp máy tính để bàn, giữ bạn luôn đăng nhập, đọc trang, tải xuống và tải lên; tự điều chỉnh nhịp khi lật trang và chuyển các trang bị chặn lại cho bạn |
| **Màn hình bạn đang xem** | Chạm hai lần vào viên nang nổi để gửi màn hình bạn đang nhìn: "nên trả lời thế nào?", "ghi lại cái này giúp tôi" |
| **Lịch hẹn và điều kiện** | "Nhắc tôi lúc 9 giờ ngày mai", "làm việc này mỗi thứ Sáu"; chạy đúng giờ trong cuộc trò chuyện gốc |
| **Can thiệp hoặc dừng bất cứ lúc nào** | Tiếp tục nói trong khi một tác vụ đang chạy để bổ sung hoặc sửa chữa (nhấn giữ để rút lại hoặc sửa một tin nhắn Aira chưa xử lý); sau khi dừng, nó báo đã làm đến đâu, và "tiếp tục" sẽ làm tiếp từ đó |
| **Nhấn giữ để nói** | Nhấn giữ ô nhập trò chuyện để nói và sửa chữ trước khi gửi; nhấn giữ viên nang nổi để nói và thả ra là gửi. Dùng nhận dạng giọng nói tích hợp của điện thoại, hoặc mô hình giọng nói của một nhà cung cấp nếu điện thoại không có |
| **Tệp và tính toán** | Một sandbox Linux cho mỗi cuộc trò chuyện, chạy Python, Node và shell trên các tệp bạn gửi (arm64) |
| **Từ máy tính của bạn** | Các công cụ AI trên máy tính của bạn có thể giao việc cho Aira qua CLI + Skill hoặc MCP ([dùng Aira từ máy tính](../computer.en.md)) |

Mặc định Aira chạy ở **chế độ Ask (Hỏi)**: nó chỉ hỏi bạn trước khi tiêu tiền (đặt hàng, thanh toán) và tự làm mọi việc còn lại. Bạn cũng có thể chuyển sang **chế độ Autonomous (Tự chủ)**. Xem [Ask và Autonomous](../modes.en.md).

Để biết mỗi tính năng hoạt động thế nào và các chi tiết sâu hơn, xem [tính năng chi tiết](../features.en.md).

<details>
<summary>Ứng dụng và trang web có sẵn kiến thức sử dụng tích hợp (không có trong danh sách vẫn dùng được)</summary>

Danh sách đầy đủ nằm trong mục [What it can do](../../README.md#what-it-can-do) của README tiếng Anh và tự cập nhật mỗi khi kiến thức tích hợp thay đổi.

</details>

<details>
<summary>Các nhà cung cấp mô hình được hỗ trợ</summary>

Các nhà cung cấp có sẵn và các loại API mà tùy chọn "Custom" (Tùy chỉnh) kết nối được đều nằm trong mục [What it can do](../../README.md#what-it-can-do) của README tiếng Anh, tự cập nhật từ mã nguồn.

Bạn cũng có thể bỏ qua khóa và đăng nhập bằng tài khoản ChatGPT (Plus / Pro), xAI / Grok hoặc OpenRouter. Các bước thêm một mô hình, và những điều cần biết về từng cách đăng nhập, nằm trong [mô hình và đăng nhập](../models.en.md).

</details>

## Dùng được trên những điện thoại nào

| | Yêu cầu |
|---|---|
| Trò chuyện, web, tệp | Android 11 trở lên |
| Điều khiển ứng dụng trên điện thoại | Android 12 trở lên; **khuyến nghị Android 14+** (màn hình nền có tiêu điểm riêng, nên không cản trở việc bạn gõ phím trên màn hình chính) |
| Sandbox Linux | Thiết bị arm64 |

Không chắc điện thoại của bạn có đáp ứng được không? Chạy Settings › Recovery & repair › **Compatibility self-check** (Cài đặt › Khôi phục & sửa chữa › **Tự kiểm tra tương thích**): 12 bước kiểm tra trong khoảng một phút, và bạn có thể [gửi kết quả cho chúng tôi](https://github.com/ganguydhrubo/Aira/issues/new?template=device-report.yml) chỉ với một chạm.

## Quyền riêng tư & an toàn

- **Không có máy chủ Aira**: những gì bạn nói và những gì trên màn hình của bạn chỉ được gửi đến nhà cung cấp mô hình bạn đã thiết lập.
- Khóa API và ký ức được lưu mã hóa trên điện thoại; các cuộc trò chuyện và bản ghi chạy tác vụ ở lại trên thiết bị; việc kiểm tra cập nhật chỉ liên hệ GitHub.
- Tự động điều khiển ứng dụng và trang web của bên thứ ba có thể vi phạm điều khoản dịch vụ của họ, vì vậy hãy tự cân nhắc. Với việc quan trọng, hãy dùng chế độ Ask và kiểm tra kết quả. Xem [tuyên bố miễn trừ](../disclaimer.en.md).

<details>
<summary><b>Câu hỏi thường gặp</b></summary>

**Gỡ lỗi không dây tự tắt?** Android tắt nó khi bạn đổi Wi-Fi hoặc khởi động lại. Nếu bạn cho phép, Aira sẽ tự bật lại; nếu vẫn không kết nối được, hãy quay lại "Set up phone control" và ghép nối lại.

**Không tìm thấy gỡ lỗi không dây trên điện thoại?** Một số hệ điều hành điện thoại không có tính năng này. Bạn có thể khởi động trợ lý điện thoại từ máy tính qua USB thay thế; xem [hướng dẫn thiết lập](../setup-guide.en.md).

**Một số màn hình không đọc hoặc chụp được?** Các trang như thanh toán và nhập mật khẩu chặn chụp màn hình, vì vậy Aira sẽ nhờ bạn tiếp quản ở đó.

**Tôi có thể dùng mô hình chạy trên máy tính của mình không?** Có: chọn "Custom" làm nhà cung cấp và nhập địa chỉ của nó trong mạng nội bộ của bạn.

**Ứng dụng có tiếng Việt không?** Có. Giao diện ứng dụng có tiếng Việt, tiếng Anh, tiếng Ả Rập, tiếng Ba Tư, tiếng Trung giản thể, tiếng Trung phồn thể, tiếng Nhật, tiếng Hàn, tiếng Tây Ban Nha, tiếng Bồ Đào Nha, tiếng Nga và tiếng Indonesia, đi theo ngôn ngữ hệ thống và đổi được trong Settings › Language (Cài đặt › Ngôn ngữ). Kiến thức tích hợp về ứng dụng và trang web được viết bằng tiếng Trung, nhưng điều đó không giới hạn bạn: mô hình đọc hiểu tốt, và bạn có thể nói chuyện với Aira bằng tiếng Việt.

**Cập nhật đến từ đâu?** Chỉ từ [Releases](https://github.com/ganguydhrubo/Aira/releases) của repo này; bạn cũng có thể nâng cấp trong ứng dụng từ About › Check for updates (Giới thiệu › Kiểm tra cập nhật).

Thêm câu trả lời trong [FAQ](../faq.en.md).

</details>

## Cộng đồng & đóng góp

- **Tài liệu**: toàn bộ tài liệu và thứ tự đọc gợi ý nằm trong [mục lục tài liệu](../README.en.md)
- **Báo lỗi**: [mở một issue](https://github.com/ganguydhrubo/Aira/issues/new/choose); trong ứng dụng, About › Send feedback (Giới thiệu › Gửi phản hồi) có thể đính kèm thông tin chẩn đoán
- **Đóng góp**: thêm kiến thức sử dụng cho một ứng dụng hoặc trang web (văn bản thuần, không cần code), hoặc đóng góp mã; xem [hướng dẫn đóng góp](../../.github/CONTRIBUTING.md)

<p align="center">⭐ Nếu Aira giúp bạn tiết kiệm thời gian, hãy cho nó một sao — để nhiều người biết đến nó hơn.</p>

## Những người đóng góp

Cảm ơn tất cả những người đã góp phần xây dựng Aira:

<a href="https://github.com/ganguydhrubo/Aira/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=ganguydhrubo/Aira&max=100&columns=12" alt="Contributors" />
</a>

## Giấy phép

[Apache-2.0](../../LICENSE) · [NOTICE](../../NOTICE) · [Thành phần bên thứ ba](../../licenses/THIRD_PARTY_NOTICES.md)
