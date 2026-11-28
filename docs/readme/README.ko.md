<!-- translated-from: README.md @ ebe4848fbb14 -->
<div align="center">

<img src="../media/logo.png" alt="Aira" width="88" />

# Aira

**휴대폰 속 AI 비서: 한마디만 하면 앱과 웹을 대신 조작해서 일을 끝내 줍니다.**

보이지 않는 백그라운드 화면에서 일하기 때문에, 그동안에도 휴대폰은 평소처럼 쓰면 됩니다.

<br/>
<a href="https://github.com/ganguydhrubo/Aira/releases/latest"><img src="https://img.shields.io/badge/APK%20%EB%8B%A4%EC%9A%B4%EB%A1%9C%EB%93%9C-Android%2011%2B-2ea043?style=for-the-badge&logo=android&logoColor=white" alt="APK 다운로드" /></a> <a href="https://github.com/ganguydhrubo/Aira"><img src="https://img.shields.io/github/stars/ganguydhrubo/Aira?style=for-the-badge&logo=github&label=Star&color=FFD700&logoColor=white" alt="Star this repo" /></a>

<a href="https://github.com/ganguydhrubo/Aira/releases"><img src="https://img.shields.io/github/downloads/ganguydhrubo/Aira/total?label=%EB%8B%A4%EC%9A%B4%EB%A1%9C%EB%93%9C&color=2ea043" alt="downloads" /></a>
<a href="https://github.com/ganguydhrubo/Aira/releases/latest"><img src="https://img.shields.io/github/v/release/ganguydhrubo/Aira?include_prereleases&label=%EB%B2%84%EC%A0%84" alt="version" /></a>
<a href="../../LICENSE"><img src="https://img.shields.io/badge/license-Apache--2.0-lightgrey" alt="license" /></a>

<br/>

[English](../../README.md) · [日本語](README.ja.md) · 한국어 · [Español](README.es.md) · [Türkçe](README.tr.md) · [Tiếng Việt](README.vi.md)

<sub>이 한국어판은 중국어(간체) 원문을 번역한 것입니다. 어색한 표현이나 원문과 다른 부분이 보이면 PR로 고쳐 주세요.</sub>

<br/>

</div>

## 왜 Aira인가

| | |
|---|---|
| 📱 **내 화면을 차지하지 않음** | 별도의 백그라운드 화면에서 앱을 열고, 탭하고, 입력하고, 스크롤합니다. 그동안 휴대폰은 평소처럼 쓰고, 보고 싶을 때만 작은 미리보기를 누르면 됩니다 |
| 🧩 **어떤 앱이든 OK** | 사람처럼 화면을 보고 버튼을 찾기 때문에 앱의 API도, 앱별 사전 연동도 필요 없습니다 |
| 🌐 **웹페이지에서 바로 부르기** | 내장 브라우저에서 보고 있는 페이지에 한마디 하면 그 페이지에서 바로 일합니다. 페이지를 건드리면 즉시 멈춥니다 |
| 🧠 **쓸수록 똑똑해짐** | 알려 준 취향을 기억하고, 끝낸 일은 자동으로 스킬로 정리해 다음번엔 더 빠르고 안정적으로 해냅니다 |
| 🔒 **데이터는 내 휴대폰에만** | Aira 서버는 없습니다. 내 모델 API 키를 쓰거나 ChatGPT, xAI, OpenRouter 계정으로 로그인하세요 |
| 🆓 **오픈 소스, 무료** | Apache-2.0, 코드 전부 공개 |

## 3단계로 시작하기

1. **설치**: [Releases](https://github.com/ganguydhrubo/Aira/releases/latest)에서 APK를 내려받습니다.
2. **모델 연결**: 설정 › 모델에서 제공업체를 골라 API 키를 입력하거나, 계정으로 바로 로그인합니다([모델과 로그인](../models.md)).
3. **휴대폰 앱을 조작하게 하려면**: 설정의 "Set up phone control" 마법사를 따라 휴대폰에서 무선 디버깅을 한 번 페어링합니다([설정 가이드](../setup-guide.md)). 채팅, 웹, 파일 작업만 할 거라면 이 단계는 건너뛰어도 됩니다.

이제 말을 걸어 보세요: "내일 아침 9시 40분이랑 10시에 알람 맞춰 줘", "매일 아침 8시에 날씨 보고 비 올 때만 알려 줘".

> 💡 컴퓨터가 옆에 있나요? 이 문장을 컴퓨터의 AI 에이전트에게 보내면 대신 설치해 줍니다:<br/>
> `https://github.com/ganguydhrubo/Aira/blob/main/docs/install-with-agent.md 의 단계에 따라, USB로 연결된 Android 휴대폰에 Aira를 설치해 줘.`

## 할 수 있는 일

| | |
|---|---|
| **앱 조작** | 긴 목록과 대화 기록 읽기, 탭과 입력, 앱으로 텍스트 공유. 로그인, 인증 코드, 결제는 사용자에게 넘깁니다 |
| **웹 사용** | 로그인 상태가 유지되는 PC 버전 내장 브라우저로 페이지를 읽고, 다운로드와 업로드도 합니다. 페이지는 적당한 속도로 넘기고, 차단 페이지가 나오면 사용자에게 넘깁니다 |
| **지금 보고 있는 화면** | 플로팅 캡슐을 두 번 탭하면 보고 있는 화면을 Aira에게 보냅니다: "이거 뭐라고 답하지?", "이거 기록해 둬" |
| **예약과 조건** | "내일 아침 9시에 알려 줘", "매주 금요일에 이거 해 줘". 시간이 되면 원래 대화에서 실행합니다 |
| **언제든 끼어들고, 언제든 멈추기** | 작업 중에도 계속 말해서 요구 사항을 더할 수 있고, 아직 반영되지 않은 말은 길게 눌러 취소하거나 수정할 수 있습니다. 멈추면 어디까지 했는지 알려 주고, "계속"이라고 하면 이어서 합니다 |
| **눌러서 말하기** | 입력창을 누른 채 말하면 글자가 들어가고, 고친 뒤 보낼 수 있습니다. 플로팅 캡슐은 길게 눌러 말하고 손을 떼면 바로 보냅니다. 휴대폰의 음성 인식을 쓰고, 없으면 제공자의 음성 모델을 고를 수 있습니다 |
| **파일과 계산** | 대화마다 Linux 샌드박스가 있어 Python, Node, 셸로 보내 준 파일을 처리합니다(arm64) |
| **컴퓨터에서도 사용** | 컴퓨터의 AI 도구가 CLI + Skill 또는 MCP로 Aira에게 일을 맡길 수 있습니다([컴퓨터에서 Aira 쓰기](../computer.md)) |

기본값은 **확인 모드**입니다. 돈이 드는 일(주문, 결제)만 먼저 물어보고 나머지는 끝까지 해냅니다. **자율 모드**로 바꿀 수도 있습니다. 자세한 내용은 [확인 모드와 자율 모드](../modes.md)를 보세요.

각 기능의 사용법과 세부 사항은 [기능 자세히 보기](../features.md)에 있습니다.

<details>
<summary>사용 지식이 준비된 앱과 사이트(목록에 없어도 그냥 쓸 수 있습니다)</summary>

전체 목록은 영어판 README의 [What it can do](../../README.md#what-it-can-do)에 있으며, 내장 지식에 맞춰 자동으로 갱신됩니다.

</details>

<details>
<summary>지원하는 모델 제공업체</summary>

내장된 제공업체 프리셋과 "Custom"으로 연결할 수 있는 API 종류는 영어판 README의 [What it can do](../../README.md#what-it-can-do)에 목록이 있으며, 코드에 맞춰 자동으로 갱신됩니다.

API 키 없이 ChatGPT(Plus / Pro), xAI / Grok, OpenRouter 계정으로 로그인할 수도 있습니다. 모델을 추가하는 방법과 서비스별 로그인 시 주의할 점은 [모델과 로그인](../models.md)에 정리해 두었습니다.

</details>

## 지원하는 휴대폰

| | 요구 사항 |
|---|---|
| 채팅, 웹, 파일 | Android 11 이상 |
| 휴대폰 앱 조작 | Android 12 이상. **Android 14 이상 권장**(백그라운드 화면이 자체 포커스를 가져서 메인 화면에서 타이핑하는 데 방해가 되지 않습니다) |
| Linux 샌드박스 | arm64 기기 |

내 휴대폰에서 될지 모르겠다면 설정 › 복구 및 수리 › **호환성 자가 점검**를 실행해 보세요. 1분 정도면 12개 항목을 확인하고, 결과를 한 번에 [저희에게 보낼 수 있습니다](https://github.com/ganguydhrubo/Aira/issues/new?template=device-report.yml).

## 개인정보와 안전

- **Aira 서버는 없습니다**: 내가 한 말과 화면 내용은 직접 설정한 모델 제공업체에만 전송됩니다.
- API 키와 기억은 암호화되어 휴대폰에 저장되고, 대화와 실행 기록은 기기에만 남습니다. 업데이트 확인은 GitHub에만 접속합니다.
- 다른 회사의 앱이나 사이트를 자동으로 조작하면 이용 약관에 어긋날 수 있으니 스스로 판단하세요. 중요한 일은 확인 모드로 하고 결과를 확인하세요. 자세한 내용은 [면책 조항](../disclaimer.md)을 보세요.

<details>
<summary><b>자주 묻는 질문</b></summary>

**무선 디버깅이 저절로 꺼져요?** Android는 Wi-Fi를 바꾸거나 재부팅하면 무선 디버깅을 끕니다. 허용해 두면 Aira가 알아서 다시 켭니다. 그래도 연결되지 않으면 "Set up phone control"로 돌아가 다시 페어링하세요.

**무선 디버깅 메뉴가 안 보여요?** 이 항목이 아예 없는 휴대폰도 있습니다. 그럴 땐 컴퓨터에서 USB로 휴대폰 도우미를 실행할 수 있습니다. [설정 가이드](../setup-guide.md)를 보세요.

**읽지 못하거나 캡처되지 않는 화면이 있어요?** 결제나 비밀번호 입력 같은 화면은 스크린샷이 막혀 있어서, Aira가 직접 처리해 달라고 요청합니다.

**내 컴퓨터에서 돌리는 모델도 쓸 수 있나요?** 네. 제공업체로 "Custom"을 고르고 로컬 네트워크 주소를 입력하면 됩니다.

**앱이 한국어를 지원하나요?** 네. 앱 화면은 한국어와 영어, 아랍어, 페르시아어, 중국어 간체, 중국어 번체, 일본어, 스페인어, 포르투갈어, 러시아어, 인도네시아어, 베트남어로 제공되며 시스템 언어를 따라갑니다(설정 › 언어에서도 바꿀 수 있습니다). Aira에게는 한국어로 말하면 됩니다. 내장된 앱·사이트 지식은 중국어로 쓰여 있지만 모델이 문제없이 읽습니다.

**업데이트는 어디서 받나요?** 이 저장소의 [Releases](https://github.com/ganguydhrubo/Aira/releases)에서만 배포합니다. 앱의 Aira 정보 › 업데이트 확인에서도 바로 업데이트할 수 있습니다.

더 많은 질문과 답은 [FAQ](../faq.md)에 있습니다.

</details>

## 커뮤니티와 기여

- **문서**: 모든 문서와 추천 읽기 순서는 [문서 목록](../README.md)에 있습니다
- **문제 신고**: [Issue 열기](https://github.com/ganguydhrubo/Aira/issues/new/choose). 앱의 Aira 정보 › 의견 보내기에서 진단 정보를 첨부할 수 있습니다
- **기여하기**: 앱이나 사이트의 사용 지식을 보태 주세요(일반 텍스트라 코드를 몰라도 됩니다). 코드 기여도 환영합니다. [기여 가이드](../../.github/CONTRIBUTING.md)를 보세요

<p align="center">⭐ Aira가 시간을 아껴 줬다면 Star를 눌러 더 많은 사람에게 알려 주세요.</p>

## 기여자

Aira에 함께해 주신 모든 분께 감사드립니다:

<a href="https://github.com/ganguydhrubo/Aira/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=ganguydhrubo/Aira&max=100&columns=12" alt="Contributors" />
</a>

## 라이선스

[Apache-2.0](../../LICENSE) · [NOTICE](../../NOTICE) · [서드파티 구성 요소](../../licenses/THIRD_PARTY_NOTICES.md)
