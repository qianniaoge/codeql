import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class UnsafeReflection {

    @RequestMapping(value = {"/service/{beanIdOrClassName}/{methodName}"}, method = {RequestMethod.POST}, consumes = {"application/json"}, produces = {"application/json"})
    public Object bad1(@PathVariable("beanIdOrClassName") String beanIdOrClassName, @PathVariable("methodName") String methodName, @RequestBody Map<String, Object> body) throws Exception {
        List<Object> rawData = null;
        try {
            rawData = (List<Object>)body.get("methodInput");
        } catch (Exception e) {
            return e;
        }
        return invokeService(beanIdOrClassName, methodName, null, rawData);
    }

    @GetMapping(value = "uf1")
    public void good1(HttpServletRequest request) throws Exception {
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add("com.example.test1");
        hashSet.add("com.example.test2");
        String className = request.getParameter("className");
        String parameterValue = request.getParameter("parameterValue");
        if (!hashSet.contains(className)){ 
            throw new Exception("Class not valid: "  + className);
        }
        try {
            Class clazz = Class.forName(className);
            Object object = clazz.getDeclaredConstructors()[0].newInstance(parameterValue); //good
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping(value = "uf2")
    public void good2(HttpServletRequest request) throws Exception {
        String className = request.getParameter("className");
        String parameterValue = request.getParameter("parameterValue");
        if (!"com.example.test1".equals(className)){
            throw new Exception("Class not valid: "  + className);
        }
        try {
            Class clazz = Class.forName(className);
            Object object = clazz.getDeclaredConstructors()[0].newInstance(parameterValue); //good
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object invokeService(String beanIdOrClassName, String methodName, MultipartFile[] files, List<Object> data) throws Exception {
        BeanFactory beanFactory = new BeanFactory();
        try {
            Object bean = null;
            String beanName = null;
            Class<?> beanClass = null;
            try {
                beanClass = Class.forName(beanIdOrClassName);
                beanName = StringUtils.uncapitalize(beanClass.getSimpleName());
            } catch (ClassNotFoundException classNotFoundException) {
                beanName = beanIdOrClassName;
            }
            try {
                bean = beanFactory.getBean(beanName);
            } catch (BeansException beansException) {
                bean = beanFactory.getBean(beanClass);
            }
            byte b;
            int i;
            Method[] arrayOfMethod;
            for (i = (arrayOfMethod = bean.getClass().getMethods()).length, b = 0; b < i; ) {
                Method method = arrayOfMethod[b];
                if (!method.getName().equals(methodName)) {
                    b++;
                    continue;
                }
                ProxygenSerializer serializer = new ProxygenSerializer();
                Object[] methodInput = serializer.deserializeMethodInput(data, files, method);
                Object result = method.invoke(bean, methodInput);
                Map<String, Object> map = new HashMap<>();
                map.put("result", serializer.serialize(result));
                return map;
            }
        } catch (Exception e) {
            return e;
        }
        return null;
    }
}

class BeansException extends Exception {

}

class BeanFactory {

    private static HashMap<String, Object> classNameMap = new HashMap<>();

    private static HashMap<Class<?>, Object> classMap = new HashMap<>();;

    static {
        classNameMap.put("xxxx", Runtime.getRuntime());
        classMap.put(Runtime.class, Runtime.getRuntime());
    }

    public Object getBean(String className) throws BeansException {
        if (classNameMap.get(className) == null) {
            throw new BeansException();
        }
        return classNameMap.get(className);
    }

    public Object getBean(Class<?> clzz) {
        return classMap.get(clzz);
    }
}

class ProxygenSerializer {

    public Object[] deserializeMethodInput(List<Object> data, MultipartFile[] files, Method method) {
        return null;
    }

    public String serialize(Object result) {
        return null;
    }
}
