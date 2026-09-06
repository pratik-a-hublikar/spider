import { useEffect, useMemo, useState } from 'react';
import salesPeople from '../data/salesPerformance.json';
import { shipmntsService } from '../services/shipmntsService';
import './SalesPerformanceMirror.css';
import './SalesPerformanceMirrorPrint.css';

const MONTHS = ['April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December', 'January', 'February', 'March'];
const SHORT_MONTHS = MONTHS.map((month) => month.slice(0, 3));
const SEASONAL_SPLIT = [.07, .08, .09, .09, .08, .08, .08, .08, .09, .09, .09, .08];

const seed = (name, month) => {
  let hash = 0;
  for (let index = 0; index < name.length; index += 1) hash = (hash * 31 + name.charCodeAt(index)) & 0xffff;
  return (hash * (month + 3)) % 100;
};

const percent = (actual, target) => (target > 0 ? Math.min(Math.round((actual / target) * 100), 999) : 0);
const money = (value) => {
  if (value >= 10000000) return `₹${(value / 10000000).toFixed(2)} Cr`;
  if (value >= 100000) return `₹${(value / 100000).toFixed(2)} L`;
  if (value >= 1000) return `₹${Math.round(value / 1000)}K`;
  return `₹${Math.round(value)}`;
};
const tone = (value) => (value >= 90 ? 'good' : value >= 70 ? 'amber' : 'bad');
const status = (value) => (value >= 90 ? 'On track ✓' : value >= 70 ? 'Monitor ⚠' : 'Below target ✕');

const getData = (person, month) => {
  const monthlyBudget = Math.round(person.annualBudget * SEASONAL_SPLIT[month]);
  const value = seed(person.name, month);
  const performanceFactor = 0.52 + (value % 55) / 100;
  const actualRevenue = Math.round(monthlyBudget * performanceFactor);
  const visitTarget = 20 + Math.round(value % 10);
  const visits = Math.round(visitTarget * (0.5 + (value % 50) / 100));
  const callTarget = 60 + Math.round(value % 30);
  const calls = Math.round(callTarget * (0.55 + (value % 40) / 100));
  const emailTarget = 40 + Math.round(value % 20);
  const emails = Math.round(emailTarget * (0.6 + (value % 35) / 100));
  const newTarget = 4;
  const newClients = Math.round(newTarget * (0.5 + (value % 60) / 100));
  const existingTarget = 8;
  const existingClients = Math.round(existingTarget * (0.65 + (value % 35) / 100));
  const approaches = visits + newClients + existingClients + Math.round(value % 3);
  const meetings = Math.round(approaches * .68);
  const quotes = Math.round(meetings * .58);
  const closed = Math.round(quotes * .33);
  const volumeTarget = 20 + Math.round(value % 15);
  const volume = Math.round(volumeTarget * performanceFactor);
  const collectionTarget = Math.round(monthlyBudget * .9);
  const collections = Math.round(actualRevenue * (.75 + (value % 20) / 100));
  const profitPercent = 8 + (value % 8);
  const profit = Math.round(actualRevenue * profitPercent / 100);
  const profitTarget = Math.round(monthlyBudget * (profitPercent + 2) / 100);
  return { monthlyBudget, actualRevenue, visitTarget, visits, callTarget, calls, emailTarget, emails, newTarget, newClients, existingTarget, existingClients, approaches, meetings, quotes, closed, volumeTarget, volume, collectionTarget, collections, profitPercent, profit, profitTarget };
};

const KpiCard = ({ label, actual, target, currency = false, accent }) => {
  const achieved = percent(actual, target);
  return <div className="spm-kpi" style={{ '--accent': accent }}><div className="spm-kpi-label">{label}</div><div className="spm-kpi-value">{currency ? money(actual) : actual}</div><div className="spm-muted">Target: {currency ? money(target) : target}</div><div className={`spm-status ${tone(achieved)}`}>{achieved}% — {status(achieved)}</div><div className="spm-progress"><span style={{ width: `${Math.min(achieved, 100)}%` }} /></div></div>;
};

const MetricRow = ({ label, actual, target, currency = false }) => {
  const achieved = percent(actual, target);
  return <div className="spm-metric"><span>{label}</span><div><strong>{currency ? money(actual) : actual}</strong><small>Budget: {currency ? money(target) : target}</small><i><b style={{ width: `${Math.min(achieved, 100)}%` }} /></i></div></div>;
};

const SectionTitle = ({ letter, title, subtitle, color }) => <div className="spm-section-title"><span style={{ background: color }}>{letter}</span><strong>{title}</strong><small>{subtitle}</small></div>;
const BlockHeader = ({ children, color }) => <div className="spm-block-header" style={{ background: color }}>● {children}</div>;
const Pill = ({ value }) => <span className={`spm-pill ${tone(value)}`}>{value}%</span>;

const BarChart = ({ items, currency = false }) => {
  const maximum = Math.max(...items.flatMap((item) => [item.actual, item.target]), 1);
  return <div className="spm-bar-chart" role="img" aria-label="Actual compared with budget chart">
    <div className="spm-chart-legend"><span className="actual">Actual</span><span className="budget">Budget</span></div>
    <div className="spm-chart-plot">
      {items.map((item) => <div className="spm-chart-group" key={item.label}>
        <div className="spm-chart-columns">
          <i title={`Actual: ${currency ? money(item.actual) : item.actual}`} style={{ height: `${Math.max((item.actual / maximum) * 100, 2)}%` }} />
          <b title={`Budget: ${currency ? money(item.target) : item.target}`} style={{ height: `${Math.max((item.target / maximum) * 100, 2)}%` }} />
        </div>
        <small>{item.label}</small>
      </div>)}
    </div>
  </div>;
};

const SalesPerformanceMirror = () => {
  const [personId, setPersonId] = useState(salesPeople[0].id);
  const [month, setMonth] = useState(3);
  const [shipmntsState, setShipmntsState] = useState({ status: 'loading', data: null, error: null });
  const person = salesPeople.find((item) => item.id === Number(personId)) ?? salesPeople[0];
  const data = useMemo(() => getData(person, month), [person, month]);
  const monthlySeries = useMemo(() => MONTHS.map((_, index) => getData(person, index)), [person]);
  const achievement = percent(data.actualRevenue, data.monthlyBudget);
  const conversion = percent(data.closed, data.approaches);
  const ytdBudget = monthlySeries.slice(0, month + 1).reduce((sum, item) => sum + item.monthlyBudget, 0);
  const ytdActual = monthlySeries.slice(0, month + 1).reduce((sum, item) => sum + item.actualRevenue, 0);
  const ytdAchievement = percent(ytdActual, ytdBudget);
  const remainingMonths = 11 - month;
  const annualGap = Math.max(0, person.annualBudget - ytdActual * (12 / (month + 1)));
  const monthlyNeed = remainingMonths > 0 ? Math.round(annualGap / remainingMonths) : 0;
  const deficit = Math.max(0, data.monthlyBudget - data.actualRevenue);
  const maxTrend = Math.max(...monthlySeries.map((item) => item.monthlyBudget));
  const activities = [
    ['Client visits', data.visitTarget, data.visits], ['Phone calls', data.callTarget, data.calls],
    ['Emails sent', data.emailTarget, data.emails], ['New customer approaches', data.newTarget, data.newClients],
    ['Existing customer visits', data.existingTarget, data.existingClients],
    ['Total customer touch-points', data.visitTarget + data.newTarget + data.existingTarget, data.visits + data.newClients + data.existingClients],
  ];
  const actions = [
    `Increase client visits by ${Math.max(0, data.visitTarget - data.visits)} above current activity.`,
    `Follow up on all ${Math.max(0, data.quotes - data.closed)} pending quotes and convert at least 50%.`,
    `On-board ${data.newTarget} new clients, prioritising A-type accounts.`,
    'Submit the weekly report every Saturday without exception.',
    'Discuss the pipeline list with the HOD at the next weekly sales meeting.',
  ];

  useEffect(() => {
    let active = true;

    shipmntsService.fetchData()
      .then((result) => {
        if (active) setShipmntsState({ status: 'succeeded', data: result, error: null });
      })
      .catch((error) => {
        if (!active) return;
        const message = error?.response?.data?.message || error?.message || 'Unable to fetch Shipmnts data.';
        setShipmntsState({ status: 'failed', data: null, error: message });
      });

    return () => { active = false; };
  }, []);

  const shipmntsProfile = shipmntsState.data?.user_profile;

  return <div className="spm-page">
    <div className="spm-controls spm-no-print">
      <label>Sales person<select value={personId} onChange={(event) => setPersonId(Number(event.target.value))}>{salesPeople.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>
      <label>View month<select value={month} onChange={(event) => setMonth(Number(event.target.value))}>{MONTHS.map((item, index) => <option key={item} value={index}>{item}</option>)}</select></label>
      <button onClick={() => window.print()}>🖨 Print / PDF</button>
    </div>
    <div className={`spm-shipmnts-status ${shipmntsState.status}`} role="status">
      {shipmntsState.status === 'loading' && 'Connecting to Shipmnts and loading data…'}
      {shipmntsState.status === 'failed' && shipmntsState.error}
      {shipmntsState.status === 'succeeded' && `Shipmnts connected${shipmntsProfile?.name ? ` as ${shipmntsProfile.name}` : ''}.`}
    </div>

    <article className="spm-mirror">
      <header className="spm-brand"><div className="spm-logo">SF</div><div><small>Monthly Performance Report — Individual Review</small><h1>Sales Performance Mirror — Post-MIS</h1></div><div className="spm-report-month"><strong>{SHORT_MONTHS[month]} 2026</strong><small>Report Month</small></div></header>
      <section className="spm-person"><div><small>Sales Executive</small><h2>{person.name}</h2><p>{person.desk} Desk · {person.department} Department · FY 2025–26 · {MONTHS[month]} Post-MIS Review</p></div><div><small>Annual Budget</small><strong>{money(person.annualBudget)}</strong><small>This Month Budget</small><strong>{money(data.monthlyBudget)}</strong></div></section>
      <nav className="spm-months spm-no-print">{SHORT_MONTHS.map((item, index) => <button className={month === index ? 'active' : ''} onClick={() => setMonth(index)} key={item}>{item}</button>)}</nav>
      <div className="spm-attention">This report reflects your <strong>actual performance for the month</strong>. Review each block, understand your gaps, and use the direction panel to plan next month.</div>

      <SectionTitle letter="A" color="#1B3A6B" title="Block 1 — Sales Activity & Effort" subtitle="What you did this month to generate business" />
      <div className="spm-kpi-grid">{activities.slice(0, 3).map(([label, target, actual], index) => <KpiCard key={label} label={label} target={target} actual={actual} accent={['#1B3A6B', '#0D6E6E', '#2E5FA3'][index]} />)}</div>
      <div className="spm-kpi-grid">{activities.slice(3).map(([label, target, actual], index) => <KpiCard key={label} label={label} target={target} actual={actual} accent={['#1A7A4A', '#C8941A', '#334155'][index]} />)}</div>
      <div className="spm-block"><BlockHeader color="#0D6E6E">Activity breakdown — actual vs target</BlockHeader><div className="spm-table-wrap"><table><thead><tr><th>Activity</th><th>Target</th><th>Actual</th><th>Achievement</th><th>Status</th></tr></thead><tbody>{activities.map(([label, target, actual]) => { const result = percent(actual, target); return <tr key={label}><td>{label}</td><td>{target}</td><td>{actual}</td><td><Pill value={result} /></td><td className={tone(result)}>{status(result)}</td></tr>; })}</tbody></table></div></div>

      <SectionTitle letter="B" color="#1A7A4A" title="Block 2 — Conversion Funnel" subtitle="How your efforts converted into closed business" />
      <div className="spm-block"><BlockHeader color="#1A7A4A">Client approach → close pipeline</BlockHeader><div className="spm-two"><div><h3>Funnel stages this month</h3>{[['Initial approaches', data.approaches], ['Meetings held', data.meetings], ['Quotations submitted', data.quotes], ['Deals closed', data.closed]].map(([label, value]) => <MetricRow key={label} label={label} actual={value} target={data.approaches} />)}</div><div><h3>Conversion & customer split</h3><MetricRow label="New clients — approached" actual={data.newClients} target={data.newTarget} /><MetricRow label="Existing clients — visited" actual={data.existingClients} target={data.existingTarget} /><div className="spm-donut" style={{ '--value': `${Math.min(conversion, 100) * 3.6}deg` }}><div><strong>{conversion}%</strong><small>target 25%</small></div></div></div></div></div>

      <SectionTitle letter="C" color="#334155" title="Block 3 — Volume & Billing" subtitle="Shipment volume, billing raised and collections" />
      <div className="spm-block"><BlockHeader color="#334155">Volume, billing & collections — actual vs budgeted</BlockHeader><div className="spm-two"><div><h3>Shipment volume (jobs)</h3><MetricRow label="Total shipments" actual={data.volume} target={data.volumeTarget} /><MetricRow label="FCL import" actual={Math.round(data.volume * .55)} target={Math.round(data.volumeTarget * .55)} /><MetricRow label="LCL import" actual={Math.round(data.volume * .25)} target={Math.round(data.volumeTarget * .25)} /><BarChart items={[{ label: 'FCL', actual: Math.round(data.volume * .55), target: Math.round(data.volumeTarget * .55) }, { label: 'LCL', actual: Math.round(data.volume * .25), target: Math.round(data.volumeTarget * .25) }, { label: 'Air/Clr', actual: Math.round(data.volume * .2), target: Math.round(data.volumeTarget * .2) }, { label: 'Total', actual: data.volume, target: data.volumeTarget }]} /></div><div><h3>Billing & collections (₹)</h3><MetricRow label="Billing raised" actual={data.actualRevenue} target={data.monthlyBudget} currency /><MetricRow label="Collections received" actual={data.collections} target={data.collectionTarget} currency /><MetricRow label="Outstanding pending" actual={Math.max(0, data.actualRevenue - data.collections)} target={data.actualRevenue} currency /><BarChart currency items={[{ label: 'Billing', actual: data.actualRevenue, target: data.monthlyBudget }, { label: 'Collected', actual: data.collections, target: data.collectionTarget }]} /></div></div></div>

      <SectionTitle letter="D" color="#C8941A" title="Block 4 — Revenue & Profitability" subtitle="Actual revenue, net profit, NP% and year-to-date" />
      <div className="spm-revenue-grid">{[
        ['Revenue — actual', money(data.actualRevenue), `Budget: ${money(data.monthlyBudget)}`, `${achievement}%`],
        ['Revenue — deficit', money(deficit), deficit ? 'Recover next month' : 'No deficit', deficit ? 'Gap' : '✓ Nil gap'],
        ['Net profit — actual', money(data.profit), `Budget: ${money(data.profitTarget)}`, `${percent(data.profit, data.profitTarget)}%`],
        ['NP margin %', `${data.profitPercent}%`, 'Budget NP%: 10%', data.profitPercent >= 10 ? 'On target' : 'Below'],
        ['YTD revenue', money(ytdActual), `YTD budget: ${money(ytdBudget)}`, `${ytdAchievement}%`],
        ['Annual gap', money(annualGap), `${remainingMonths} months left`, `${money(monthlyNeed)}/mo needed`],
      ].map(([label, value, note, result]) => <div key={label}><small>{label}</small><strong>{value}</strong><span>{note}</span><b>{result}</b></div>)}</div>
      <div className="spm-block spm-trend"><BlockHeader color="#1B3A6B">Month-by-month revenue — actual vs budget (₹ in Lakhs)</BlockHeader><div className="spm-trend-bars">{monthlySeries.map((item, index) => <div key={SHORT_MONTHS[index]}><div className="spm-bars"><i style={{ height: `${(item.monthlyBudget / maxTrend) * 100}%` }} /><b className={index === month ? 'selected' : ''} style={{ height: `${index <= month ? (item.actualRevenue / maxTrend) * 100 : (item.monthlyBudget / maxTrend) * 100}%` }} /></div><small className={index === month ? 'selected' : ''}>{SHORT_MONTHS[index]}</small></div>)}</div></div>

      <section className="spm-gap"><BlockHeader color="#92660A">▲ Gap direction — what you must achieve next month to stay on track</BlockHeader><div className="spm-gap-grid">{[['Revenue deficit this month', money(deficit), 'Must recover next month'], ['Annual gap remaining', money(annualGap), `Across ${remainingMonths} months remaining`], ['Monthly revenue needed', money(monthlyNeed), 'To close annual budget gap'], ['Extra visits needed', Math.max(0, data.visitTarget - data.visits), 'Above current activity'], ['Extra calls needed', Math.max(0, data.callTarget - data.calls), 'To reach call target'], ['Deals to close', Math.max(0, data.quotes - data.closed), 'From pending quotes']].map(([label, value, note]) => <div key={label}><small>{label}</small><strong>{value}</strong><span>{note}</span></div>)}</div><div className="spm-direction">You are at <strong>{achievement}%</strong> of your {MONTHS[month]} target. {achievement >= 90 ? 'Maintain this momentum and protect existing client relationships.' : `Recover ${money(deficit)} by increasing visits and converting the ${Math.max(0, data.quotes - data.closed)} pending quotes.`}</div></section>
      <section className="spm-actions"><h3>Recommended priority actions for next month</h3>{actions.map((action, index) => <div key={action}><b>{index + 1}</b><span>{action}</span></div>)}</section>
      <section className="spm-signoff"><h3>Acknowledgement & commitment sign-off</h3>{['Sales Executive name & signature', 'Date reviewed', 'HOD remarks', 'HOD signature & date'].map((item) => <div key={item}><small>{item}</small></div>)}</section>
      <footer><span><strong>Sanfreight Logistics Pvt. Ltd.</strong><br />Sales Performance Mirror · FY 2025–26 · Monthly Post-MIS Report</span><small>Confidential · Internal Use Only</small></footer>
    </article>
  </div>;
};

export default SalesPerformanceMirror;
