var stockSeqNums = {};
function updateTable(result) {
    let stockName = result.symbol;
    let cells = document.getElementById(stockName).cells;
    stockSeqNums[stockName] = stockSeqNums[stockName] || {};

    if (result.seqnum) {
        let diffSN = +result.seqnum - (stockSeqNums[stockName] || 0);
        cells[1].innerHTML = `${result.seqnum} <br> (${diffSN})`;
        stockSeqNums[stockName] = +result.seqnum;
    }

    if (result.bid)     { cells[2].innerHTML = result.bid; }
    if (result.bidsize) { cells[3].innerHTML = result.bidsize; }
    if (result.ask)     { cells[4].innerHTML = result.ask; }
    if (result.asksize) { cells[5].innerHTML = result.asksize; }
    if (result.last)    { cells[6].innerHTML = `<div class="updated">${result.last}</div>`; }

    if (result.timestamp) {
        let timestamp = new Date(+result.timestamp);
        let diff = new Date().getTime() - timestamp;
        cells[7].innerHTML = `${timestamp.toLocaleTimeString('it-IT')} <br> (${diff} ms)`;
    }
}