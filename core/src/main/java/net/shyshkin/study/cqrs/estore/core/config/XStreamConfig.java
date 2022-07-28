package net.shyshkin.study.cqrs.estore.core.config;

import com.thoughtworks.xstream.XStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XStreamConfig {

    @Bean
    public XStream xStream() {
        XStream xStream = new XStream();

        xStream.allowTypesByWildcard(new String[] {
                "net.shyshkin.study.cqrs.estore.**"
        });
        return xStream;
    }
}
