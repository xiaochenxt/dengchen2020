// 使用示例
let data = [];
fetchGet("/api/chunk", {}, function (str, done) {
    try {
        const json = JSON.parse(str);
        console.log("收到数据块:", json);
        data.push(json);
        if(done) console.log(data);
    } catch (e) {
        console.error("解析JSON失败:", e, "数据:", str);
    }
})

function fetchData(url, options, handleFunc) {
    fetch(url, options)
        .then(res => {
            const reader = res.body.getReader();
            const decoder = new TextDecoder("utf-8");
            let remaining = "";
            let pendingBatch = [];

            function readStream() {
                reader.read().then(({ done, value }) => {
                    const text = value ? decoder.decode(value, { stream: true }) : "";
                    const data = remaining + text;
                    const lines = data.split("\n");
                    remaining = lines.pop() || "";
                    // 提取当前批次的有效数据
                    const currentBatch = lines.filter(line => line.trim());
                    if (!done) {
                        // 非最后批次：处理上一批暂存数据，当前批次暂存
                        if (pendingBatch.length > 0) {
                            pendingBatch.forEach(line => handleFunc(line, false));
                        }
                        pendingBatch = currentBatch;
                        readStream();
                        return;
                    }
                    // 最后批次处理
                    const allData = [];
                    // 添加之前暂存的批次
                    if (pendingBatch.length > 0) {
                        allData.push(...pendingBatch);
                    }
                    // 添加当前批次
                    if (currentBatch.length > 0) {
                        allData.push(...currentBatch);
                    }
                    // 添加剩余缓存数据
                    if (remaining.trim()) {
                        allData.push(remaining);
                    }
                    // 处理所有数据
                    allData.forEach((line, index) => {
                        const isLast = index === allData.length - 1;
                        handleFunc(line, isLast);
                    });
                    console.log("流传输完成");
                }).catch(error => {
                    console.error("流读取错误:", error);
                });
            }

            readStream();
        })
        .catch(error => {
            console.error("请求失败:", error);
        });
}

function fetchGet(url, header, handleFunc) {
    fetchData(url, {
        method: "GET",
        header: header,
    }, handleFunc);
}

function fetchPost(url, header, handleFunc) {
    fetchData(url, {
        method: "POST",
        headers: header
    }, handleFunc);
}