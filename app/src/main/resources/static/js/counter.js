const counter = {
    messages : 0,
    symbols : 0,
    update : function(length) {
        this.messages++;
        this.symbols += length;
    },
    clear : function() {
        this.messages = 0;
        this.symbols = 0;
    }
};

setInterval(() => {
    document.getElementById("messagesPerSecond").innerHTML = `${counter.messages}`;
    document.getElementById("symbolsPerSecond").innerHTML = `${counter.symbols}`;
    counter.clear();
}, 1000);