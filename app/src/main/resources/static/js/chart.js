const limit = 8000,  // points at the chart for a line
    interval = 20 * 1000,
    now = Date.now(),
    startTime = new Date(now - interval);

const width = 800,
    height = 300;

const groups = {
    STOCK_AAPL: {
        value: 0,
        color: 'blue',
        data: d3.range(limit).map(() => {return {value:0, time: startTime}})
    },
    STOCK_IBM: {
        value: 0,
        color: 'red',
        data: d3.range(limit).map(() => {return {value:0, time: startTime}})
    },
    STOCK_EPAM: {
        value: 0,
        color: 'green',
        data: d3.range(limit).map(() => {return {value:0, time: startTime}})
    },
    STOCK_TSLA: {
        value: 0,
        color: 'black',
        data: d3.range(limit).map(() => {return {value:0, time: startTime}})
    }
};

const x = d3.time.scale()
    .domain([startTime, new Date(now)])
    .range([0, width]);

const y = d3.scale.linear()
    .domain([0, 100])
    .range([height, 0]);

const line = d3.svg.line()
    .x((d) => {
        let xValue = x(d.time);
        return xValue > width ? width : xValue > 0 ? xValue : 0;
    })
    .y((d) => y(d.value));

const svg = d3.select('.graph').append('svg')
    .attr('class', 'chart')
    .attr('width', width)
    .attr('height', height + 50);

const axis = svg.append('g')
    .attr('class', 'x axis')
    .attr('transform', 'translate(0,' + height + ')')
    .call(x.axis = d3.svg.axis().scale(x).orient('bottom'));

const paths = svg.append('g');

for (let name in groups) {
    let group = groups[name];
    group.path = paths.append('path')
        .data([group.data])
        .attr('class', name + ' group')
        .style('stroke', group.color);
}

function update(date) {
    for (let name in groups) {
        groups[name].path.attr('d', line);
    }

    // Shift domain
    x.domain([new Date(date - interval), date]);

    // Slide x-axis left
    axis.transition()
        .ease('ease')
        .call(x.axis)

    paths.transition().ease('ease');
}

const throttledUpdate =_.throttle(update, 1000); // updating chart only every 500 ms, not to kill UI

function updateChart(result) {
    let stockName = result.symbol,
        value = result.ask,
        date = new Date(+result.timestamp);

    // Validating data
    if (result.ask && result.timestamp) {
        for (let name in groups) {
            // Add new values
            let group = groups[name],
                lastItem = group.data[group.data.length - 1],
                updated = stockName === name ? {time: date, value: value} : lastItem;

            if (lastItem.time === startTime) {
                lastItem.time = date;
            }

            group.data.push(updated);
            groups[name].data.shift();
        }

        throttledUpdate(date);
    } else {
        console.error(`there are no ask (${result.ask}) or timestamp (${result.timestamp}). symbol ${result.symbol} `)
    }
}