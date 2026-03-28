package com.ensnif.lanime.domain.user.service

import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class EmailService(private val mailSender: JavaMailSender) {

    fun sendVerificationMail(to: String, code: String): Mono<Void> {
        return Mono.fromRunnable<Void> {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setTo(to)
            helper.setSubject("[Lanime] 이메일 인증 안내")
            helper.setText(buildHtmlTemplate(code), true)

            mailSender.send(message)
        }.subscribeOn(Schedulers.boundedElastic()) // 블로킹 작업인 메일 전송을 별도 스레드에서 처리
    }

    fun sendPasswordResetMail(to: String, token: String): Mono<Void> {
        return Mono.fromRunnable<Void> {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setTo(to)
            helper.setSubject("[Lanime] 비밀번호 재설정 안내")
            helper.setText(buildPasswordResetTemplate(token), true)

            mailSender.send(message)
        }.subscribeOn(Schedulers.boundedElastic())
    }

    private fun buildPasswordResetTemplate(token: String): String {
        return """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <title>비밀번호 재설정 안내</title>
          <style>
            @media only screen and (max-width: 600px) {
              .container { width: 90% !important; padding: 20px !important; }
              .header { font-size: 20px !important; padding: 16px !important; }
              .code-box { font-size: 24px !important; padding: 16px 32px !important; }
              .text { font-size: 14px !important; }
            }
          </style>
        </head>
        <body style="margin:0;padding:0;background-color:#f7f5ff;font-family:Arial, sans-serif;">
          <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#ffffff;">
            <tr><td align="center">
              <table class="container" width="600" cellpadding="0" cellspacing="0" style="margin:40px 0;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);width:600px;">
                <tr>
                  <td class="header" style="background-color:#b473f9;color:#ffffff;padding:20px;font-size:24px;text-align:center;">
                    비밀번호 재설정 안내
                  </td>
                </tr>
                <tr>
                  <td style="background-color:#ffffff;padding:40px;text-align:center;">
                    <p class="text" style="font-size:16px;color:#333333;line-height:1.5;">
                      안녕하세요!<br/>
                      아래 인증 코드를 입력하여 비밀번호를 재설정해 주세요.
                    </p>
                    <div class="code-box" style="display:inline-block;margin:20px auto;padding:24px 48px;
                                background-color:#b473f9;color:#ffffff;font-size:32px;font-weight:bold;
                                border-radius:8px;letter-spacing:6px;">
                      $token
                    </div>
                    <p class="text" style="font-size:14px;color:#777777;margin-top:30px;">
                      이 코드는 발송 후 15분 동안 유효합니다.<br/>
                      본인이 요청하지 않은 경우 이 메일을 무시해 주세요.
                    </p>
                  </td>
                </tr>
              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """.trimIndent()
    }

    private fun buildHtmlTemplate(code: String): String {
        return """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <title>인증번호 안내</title>
          <style>
            @media only screen and (max-width: 600px) {
              .container { width: 90% !important; padding: 20px !important; }
              .header { font-size: 20px !important; padding: 16px !important; }
              .code-box { font-size: 24px !important; padding: 16px 32px !important; }
              .text { font-size: 14px !important; }
            }
          </style>
        </head>
        <body style="margin:0;padding:0;background-color:#f7f5ff;font-family:Arial, sans-serif;">
          <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#ffffff;">
            <tr><td align="center">
              <table class="container" width="600" cellpadding="0" cellspacing="0" style="margin:40px 0;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);width:600px;">
                <tr>
                  <td class="header" style="background-color:#b473f9;color:#ffffff;padding:20px;font-size:24px;text-align:center;">
                    이메일 인증 안내
                  </td>
                </tr>
                <tr>
                  <td style="background-color:#ffffff;padding:40px;text-align:center;">
                    <p class="text" style="font-size:16px;color:#333333;line-height:1.5;">
                      안녕하세요!<br/>
                      아래 5자리 인증번호를 입력해 주세요.
                    </p>
                    <div class="code-box" style="display:inline-block;margin:20px auto;padding:24px 48px;
                                background-color:#b473f9;color:#ffffff;font-size:32px;font-weight:bold;
                                border-radius:8px;letter-spacing:6px;">
                      $code
                    </div>
                    <p class="text" style="font-size:14px;color:#777777;margin-top:30px;">
                      이 인증번호는 발송 후 5분 동안 유효합니다.
                    </p>
                  </td>
                </tr>
              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """.trimIndent()
    }
}