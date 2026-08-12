package io.github.dengchen2020.core.utils;

import com.google.i18n.phonenumbers.MetadataLoader;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;
import java.util.Set;

/**
 * 国际电话号码的实用程序。功能包括格式化、解析和验证。</br>
 * 如果您使用此库，并希望收到有关重要更改的通知，请注册
 * 我们的 <a href="https://groups.google.com/forum/#!aboutgroup/libphonenumber-discuss">邮件列表</a>。
 * 注意：此类中的许多方法都需要区域代码字符串。这些必须使用
 * CLDR 双字母区域代码格式。这些应该是大写的。代码列表
 * 可以在这里找到：
 * <a href="http://www.unicode.org/cldr/charts/30/supplemental/territory_information.html">区域代码</a>
 * @author xiaochen
 * @since 2025/8/21
 */
public abstract class PhoneNumberUtils {

    private static final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    /**
     * 获取一个 {@link PhoneNumberUtil} 实例来进行国际电话号码格式化，
     * 解析或验证。实例将加载所有电话号码元数据。
     *
     * <p>{@link PhoneNumberUtil} 作为单例实现。因此，调用 getInstance
     * 多次只会导致创建一个实例。
     *
     * @return PhoneNumberUtil 实例
     */
    public static PhoneNumberUtil getInstance() {
        return phoneNumberUtil;
    }

    /**
     * 规范化表示电话号码的字符串。这会转换宽 ASCII 和
     * 阿拉伯-印度数字到欧洲数字，并剥离标点符号和字母字符。
     *
     * @param number 代表电话号码的字符串
     * @return 电话号码的规范化字符串版本
     */
    public static String normalizeDigitsOnly(CharSequence number) {
        return PhoneNumberUtil.normalizeDigitsOnly(number);
    }

    /**
     * 规范化表示电话号码的字符串。这将剥离所有字符
     * 在手机键盘上不可折叠（包括所有非 ASCII 数字）。
     *
     * @param number 代表电话号码的字符串
     * @return 电话号码的规范化字符串版本
     */
    public static String normalizeDiallableCharsOnly(CharSequence number) {
        return PhoneNumberUtil.normalizeDiallableCharsOnly(number);
    }

    /**
     * 将数字中的所有字母字符转换为键盘上各自的数字，但保留
     * 现有格式。
     */
    public static String convertAlphaCharactersInNumber(CharSequence number) {
        return PhoneNumberUtil.convertAlphaCharactersInNumber(number);
    }

    /**
     * 返回所提供国家/地区呼叫代码的移动令牌（如果有），否则
     * 返回一个空字符串。移动令牌是拨号时插入区号前的号码
     * 来自该国家/地区的手机号码。
     *
     * @param countryCallingCode 我们想要移动令牌的国家/地区呼叫代码
     * @return 给定国家/地区呼叫代码的移动令牌（字符串）
     */
    public static String getCountryMobileToken(int countryCallingCode) {
        return PhoneNumberUtil.getCountryMobileToken(countryCallingCode);
    }

    /**
     * 创建一个新的{@link PhoneNumberUtil}实例来执行国际电话号码
     * 格式化、解析或验证。实例通过以下方式加载所有元数据
     * 使用指定的 metadataLoader。
     *
     * <p>此方法仅应用于您想要管理自己的极少数情况
     * 元数据加载。多次调用此方法是非常昂贵的，因为每次
     * 从头开始创建一个新实例。如有疑问，请使用 {@link #getInstance}。
     *
     * @param metadataLoader 自定义元数据加载器。这不应为 null
     * @return PhoneNumberUtil 实例
     */
    public static PhoneNumberUtil createInstance(MetadataLoader metadataLoader) {
        return PhoneNumberUtil.createInstance(metadataLoader);
    }

    /**
     * 解析字符串并将其作为原始缓冲区格式的电话号码返回。方法相当
     * 宽松，并在输入文本（原始输入）中查找数字，并且不检查
     * 字符串绝对只是一个电话号码。为此，它忽略了标点符号和空格，
     * 以及数字前的任何文本（例如前导“Tel： ”）并修剪非数字位。
     * 它将接受任何格式（E164、国内、国际等）的号码，假设它可以
     * 使用提供的 defaultRegion 进行解释。它还尝试转换任何字母字符
     * 如果它认为这是“1800 MICROSOFT”类型的虚名号码，则将其转换为数字。
     *
     * <p> @link如果
     * 数字不被视为可能的数字。请注意，验证数字是否
     * 实际上是未执行特定区域的有效数字。这是可以做到的
     * 与 {@link PhoneNumberUtil#isValidNumber} 分开。
     *
     * <p> 请注意，此方法将电话号码规范化，以便可以有不同的表示形式
     * 易于比较，无论最初输入的形式是什么（例如，国家、
     * 国际）。如果要记录有关正在解析的数字的上下文，例如原始数字
     * 输入的输入、国家/地区代码的派生方式等，然后调用 {@link PhoneNumberUtil#parseAndKeepRawInput(CharSequence, String)} 代替。
     *
     * @param numberToParse 我们尝试解析的数字。这可以包含格式，例如
     * 作为 +、（ 和 -，以及电话号码扩展名。也可以在RFC3966提供
     *格式。
     * @param defaultRegion 我们期望该数字来自的 defaultRegion 区域。仅在以下情况下使用此功能
     * 正在解析的数字不是以国际格式书写的。country_code
     * 在这种情况下，数字将存储为提供的默认区域的数字。如果数字
     * 保证以“+”开头，后跟国家/地区呼叫代码，然后是 RegionCode.ZZ
     * 或 null 可以提供。
     * @return 一个充满解析号码的电话号码原型缓冲区
     * 如果字符串不被视为可行的电话号码，则@throws NumberParseException（例如
     * 太少或太多的数字），或者如果没有提供默认区域并且数字不在
     * 国际格式（不以 + 开头）
     */
    public static Phonenumber.PhoneNumber parse(CharSequence numberToParse, String defaultRegion)
            throws NumberParseException {
        return  phoneNumberUtil.parse(numberToParse, defaultRegion);
    }

    /**
     * 解析字符串并将其作为原始缓冲区格式的电话号码返回。方法相当
     * 宽松，并在输入文本（原始输入）中查找数字，并且不检查
     * 字符串绝对只是一个电话号码。为此，它忽略了标点符号和空格，
     * 以及数字前的任何文本（例如前导“Tel： ”）并修剪非数字位。
     * 它将接受任何格式（E164、国内、国际等）的号码，假设它可以
     * 使用提供的 defaultRegion 进行解释。它还尝试转换任何字母字符
     * 如果它认为这是“1800 MICROSOFT”类型的虚名号码，则将其转换为数字。
     *
     * <p> @link如果
     * 数字不被视为可能的数字。请注意，验证数字是否
     * 实际上是未执行特定区域的有效数字。这是可以做到的
     * 与 {@link PhoneNumberUtil#isValidNumber} 分开。
     *
     * <p> 请注意，此方法将电话号码规范化，以便可以有不同的表示形式
     * 易于比较，无论最初输入的形式是什么（例如，国家、
     * 国际）。如果要记录有关正在解析的数字的上下文，例如原始数字
     * 输入的输入、国家/地区代码的派生方式等，然后调用 {@link PhoneNumberUtil#parseAndKeepRawInput(CharSequence, String)} 代替。
     *
     * @param numberToParse 我们尝试解析的数字。这可以包含格式，例如
     * 作为 +、（ 和 -，以及电话号码扩展名。也可以在RFC3966提供
     *格式。
     * @return 一个充满解析号码的电话号码原型缓冲区
     * 如果字符串不被视为可行的电话号码，则@throws NumberParseException（例如
     * 太少或太多的数字），或者如果没有提供默认区域并且数字不在
     * 国际格式（不以 + 开头）
     */
    public static Phonenumber.PhoneNumber parse(CharSequence numberToParse)
            throws NumberParseException {
        return  phoneNumberUtil.parse(numberToParse, null);
    }

    /**
     * Returns 返回库中所有全局网络调用代码的元数据。
     *
     * @return  图书馆支持的每个非地理实体的无序国家呼叫代码集合
     */
    public static Set<Integer> getSupportedGlobalNetworkCallingCodes() {
        return phoneNumberUtil.getSupportedGlobalNetworkCallingCodes();
    }

    /**
     * 返回库中所有区域的元数据
     *
     * @return 为图书馆支持的每个地理区域，由两个字母组成的无序区域代码集合
     */
    public static Set<String> getSupportedRegions() {
        return phoneNumberUtil.getSupportedRegions();
    }

    /**
     * 返回库中所有国家呼叫代码的元数据，涵盖非地理实体（全球网络呼叫代码）和用于地理实体的代码。
     * 例如，这可以用来填充一个国家呼叫代码下拉框，用于电话号码小部件。
     *
     * @return 图书馆支持的每个地理和非地理实体的无序国家呼叫代码集合
     */
    public static Set<Integer> getSupportedCallingCodes() {
        return phoneNumberUtil.getSupportedCallingCodes();
    }

    /**
     * 返回给定区域的区域代码 </br>
     * 这个相比直接写{@link Locale#getCountry()}只是语义上更明确罢了）。
     * @param locale
     * @return
     */
    public static String getRegionCode(Locale locale) {
        return locale.getCountry();
    }

    /**
     * 返回特定地区的国家呼叫代码。例如，中国是86，俄罗斯是7，美国是1，新西兰是64。
     *
     * @param regionCode 我们希望获得呼叫代码的国家
     * @return 该地区的国家呼叫代码，由regionCode表示
     */
    public static int getCountryCodeForRegion(String regionCode) {
        return phoneNumberUtil.getCountryCodeForRegion(regionCode);
    }

    /**
     * 获取给定区域的移动号码示例。
     *
     * @param defaultRegion 代表区域的字符串 例如：
     * @return 移动号码的示例
     */
    public static Phonenumber.PhoneNumber getExampleMobileNumber(String defaultRegion) {
        return phoneNumberUtil.getExampleNumberForType(defaultRegion, PhoneNumberUtil.PhoneNumberType.MOBILE);
    }

    /**
     * 获取给定区域的移动号码长度（不包含国家前缀或任何格式）。
     *
     * @param regionCode 代表区域的字符串
     * @return 移动号码的长度
     */
    public static int getMobileNumberLength(String regionCode) {
        return phoneNumberUtil.getNationalSignificantNumber(getExampleMobileNumber(regionCode)).length();
    }

}
