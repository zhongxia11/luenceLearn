package http;

import org.junit.Assert;
import org.junit.Test;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * java urlencode时出现空格无法解码问题
 * 注：url只需对参数进行编码
 * <p>
 *一个URL的基本组成部分包括协议(scheme),域名，端口号，路径和查询字符串（路径参数和锚点标记就暂不考虑了）。
 * 路径和查询字符串之间用问号?分离。例如http://www.example.com/index?param=1，路径为index，
 * 查询字符串(Query String)为param=1。URL中关于空格的编码正是与空格所在位置相关：
 * 空格被编码成加号+的情况只会在查询字符串部分出现，而被编码成%20则可以出现在路径和查询字符串中。
 * 造成这种混乱局面的原因在于：W3C标准规定，当Content-Type为application/x-www-form-urlencoded时，
 * URL中查询参数名和参数值中空格要用加号+替代，所以几乎所有使用该规范的浏览器在表单提交后，
 * URL查询参数中空格都会被编成加号+。
 * <p 而在另一份规范(RFC 2396，定义URI)里, URI里的保留字符都需转义成%HH格式(Section 3.4 Query Component)，
 * <p 因此空格会被编码成%20，加号+本身也作为保留字而被编成%2B，对于某些遵循RFC 2396标准的应用来说
 * <p 它可能不接受查询字符串中出现加号+，认为它是非法字符。
 * 所以一个安全的举措是URL中统一使用%20来编码空格字符。
 * <p Java中的URLEncoder，它的encode方法会把空格编成加号+，把原来的+号编码成%2B
 * <p 与之对应的是，URLDecoder的decode方法会把加号+和%20都解码为空格，
 * 在调用URLEncoder.encode对URL进行编码后(所有加号+已被编码成%2B)，
 * 再调用replaceAll(“\+”, “%20″)，将所有加号+替换为%20
 * <p 上述问题和sdk有关，有的api可能不需要再做一次替换
 *
 * @Author: stephen
 * @Date: 2019/4/29 10:14
 */
public class UrlEncodeTest {

    @Test
    public void test(){

        String url = "+四部-大数据安全威胁情报与指挥控制平台 - 产品立项报告.pdf";
        String encodeUrl = null;
        try {
            encodeUrl = URLEncoder.encode(url,"utf-8");
            Assert.assertTrue(encodeUrl.contains("%2B"));
            Assert.assertTrue(encodeUrl.contains("+"));
            encodeUrl = encodeUrl.replaceAll("\\+","%20");
            Assert.assertTrue(!encodeUrl.contains("+"));
            System.out.println(encodeUrl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }






}
