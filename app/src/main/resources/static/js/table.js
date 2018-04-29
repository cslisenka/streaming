var stockSeqNums = {};
function updateTable(result) {
    let stockName = result.symbol;
    let cells = document.getElementById(stockName).cells;
    stockSeqNums[stockName] = stockSeqNums[stockName] || {};

    if (result.seqnum) {
        let diffSN = +result.seqnum - (stockSeqNums[stockName] || 0);
        cells[1].innerHTML = `1 of ${diffSN}`;
        stockSeqNums[stockName] = +result.seqnum;
    }

    if (result.timestamp) {
        let timestamp = new Date(+result.timestamp);
        let diff = new Date().getTime() - timestamp;
        cells[2].innerHTML = `${diff} ms`;
    }

    setIfChanged(cells[3], result.bid);
    setIfChanged(cells[4], result.bidsize);
    setIfChanged(cells[5], result.ask);
    setIfChanged(cells[6], result.asksize);
    setIfChanged(cells[7], result.last);
}

function setIfChanged(row, value) {
    if (value && row.innerHTML != `<div class="updated">${value}</div>`) {
        row.innerHTML = `<div class="updated">${value}</div>`;
    }
}