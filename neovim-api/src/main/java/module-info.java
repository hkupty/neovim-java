module neovimapi {
    exports com.ensarsarajcic.neovim.java.api;
    exports com.ensarsarajcic.neovim.java.api.buffer;
    exports com.ensarsarajcic.neovim.java.api.tabpage;
    exports com.ensarsarajcic.neovim.java.api.window;
    exports com.ensarsarajcic.neovim.java.api.types.api;
    exports com.ensarsarajcic.neovim.java.api.types.msgpack;
    exports com.ensarsarajcic.neovim.java.api.types.apiinfo;
    exports com.ensarsarajcic.neovim.java.api.util;
    opens com.ensarsarajcic.neovim.java.api;
    opens com.ensarsarajcic.neovim.java.api.buffer;
    opens com.ensarsarajcic.neovim.java.api.tabpage;
    opens com.ensarsarajcic.neovim.java.api.window;
    opens com.ensarsarajcic.neovim.java.api.types.api;
    opens com.ensarsarajcic.neovim.java.api.types.msgpack;
    opens com.ensarsarajcic.neovim.java.api.types.apiinfo;
    opens com.ensarsarajcic.neovim.java.api.util;

    requires corerpc;
    requires reactivecorerpc;
    requires jackson.annotations;
    requires msgpack.core;
    requires jackson.dataformat.msgpack;
    requires slf4j.api;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
}