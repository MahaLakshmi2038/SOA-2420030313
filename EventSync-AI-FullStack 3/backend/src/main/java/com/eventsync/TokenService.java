package com.eventsync;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service public class TokenService {
 @Value("${eventsync.jwt-secret}") private String secret;
 @PostConstruct void validateSecret(){ if(secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) throw new IllegalStateException("eventsync.jwt-secret must contain at least 32 bytes."); }
 public String issue(AppUser user) { String payload=user.email+"|"+user.role+"|"+(Instant.now().getEpochSecond()+86400)+"|"+user.tokenVersion; String data=Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8)); return data+"."+sign(data); }
 public String[] verify(String token) { try { String[] parts=token.split("\\."); if(parts.length!=2 || !constantTimeEquals(sign(parts[0]),parts[1])) return null; String payload=new String(Base64.getUrlDecoder().decode(parts[0]),StandardCharsets.UTF_8); String[] fields=payload.split("\\|",-1); if(fields.length!=4 || Long.parseLong(fields[2])<Instant.now().getEpochSecond()) return null; return fields; } catch(Exception e) { return null; } }
 private String sign(String data) { try { Mac mac=Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256")); return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8))); } catch(Exception e) { throw new IllegalStateException(e); } }
 private boolean constantTimeEquals(String a,String b) { return java.security.MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),b.getBytes(StandardCharsets.UTF_8)); }
}
